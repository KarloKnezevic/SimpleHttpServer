package com.example.simplehttpserver.script.runtime;

import com.example.simplehttpserver.script.ast.ExpressionToken;
import com.example.simplehttpserver.script.ast.ExpressionTokenType;
import com.example.simplehttpserver.script.ast.ScriptDocumentNode;
import com.example.simplehttpserver.script.ast.ScriptEchoNode;
import com.example.simplehttpserver.script.ast.ScriptForLoopNode;
import com.example.simplehttpserver.script.ast.ScriptNode;
import com.example.simplehttpserver.script.ast.ScriptTextBlockNode;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Executes a parsed script AST.
 */
public class ScriptEngine {

    private final ObjectMultistack multistack = new ObjectMultistack();

    public void execute(ScriptDocumentNode document, ScriptExecutionContext context) {
        for (ScriptNode child : document.children()) {
            executeNode(child, context);
        }
    }

    private void executeNode(ScriptNode node, ScriptExecutionContext context) {
        if (node instanceof ScriptTextBlockNode textNode) {
            context.write(textNode.text());
            return;
        }

        if (node instanceof ScriptEchoNode echoNode) {
            executeEcho(echoNode, context);
            return;
        }

        if (node instanceof ScriptForLoopNode forNode) {
            executeFor(forNode, context);
            return;
        }

        throw new ScriptRuntimeException("Unknown script node type: " + node.getClass().getSimpleName());
    }

    private void executeFor(ScriptForLoopNode forNode, ScriptExecutionContext context) {
        Object start = resolveTokenValue(forNode.startExpression());
        Object end = resolveTokenValue(forNode.endExpression());
        Object step = resolveTokenValue(forNode.stepExpression());

        String variableName = forNode.variableName();
        multistack.push(variableName, new ValueWrapper(start));

        while (multistack.peek(variableName).numCompare(end) <= 0) {
            for (ScriptNode child : forNode.children()) {
                executeNode(child, context);
            }
            multistack.peek(variableName).increment(step);
        }

        multistack.pop(variableName);
    }

    private void executeEcho(ScriptEchoNode echoNode, ScriptExecutionContext context) {
        Deque<Object> stack = new ArrayDeque<>();

        for (ExpressionToken token : echoNode.tokens()) {
            switch (token.type()) {
                case INTEGER -> stack.push(Integer.parseInt(token.text()));
                case DOUBLE -> stack.push(Double.parseDouble(token.text()));
                case STRING -> stack.push(token.text());
                case VARIABLE -> stack.push(multistack.peek(token.text()).getValue());
                case OPERATOR -> applyOperator(stack, token.text());
                case FUNCTION -> applyFunction(stack, token.text(), context);
            }
        }

        List<Object> values = new ArrayList<>(stack);
        Collections.reverse(values);
        for (Object value : values) {
            context.write(String.valueOf(value));
        }
    }

    private void applyOperator(Deque<Object> stack, String operatorSymbol) {
        if (stack.size() < 2) {
            throw new ScriptRuntimeException("Operator '" + operatorSymbol + "' requires two operands.");
        }

        Object right = stack.pop();
        Object left = stack.pop();

        ValueWrapper result = new ValueWrapper(left);
        switch (operatorSymbol) {
            case "+" -> result.increment(right);
            case "-" -> result.decrement(right);
            case "*" -> result.multiply(right);
            case "/" -> result.divide(right);
            default -> throw new ScriptRuntimeException("Unsupported operator: " + operatorSymbol);
        }

        stack.push(result.getValue());
    }

    private void applyFunction(Deque<Object> stack, String functionToken, ScriptExecutionContext context) {
        String functionName = functionToken.startsWith("@")
                ? functionToken.substring(1).toLowerCase(Locale.ROOT)
                : functionToken.toLowerCase(Locale.ROOT);

        switch (functionName) {
            case "sin" -> {
                double degrees = toDouble(pop(stack, "@sin argument"));
                stack.push(Math.sin(Math.toRadians(degrees)));
            }
            case "decfmt" -> {
                String pattern = String.valueOf(pop(stack, "@decfmt pattern"));
                Object value = pop(stack, "@decfmt value");
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                stack.push(decimalFormat.format(toDouble(value)));
            }
            case "dup" -> {
                Object value = pop(stack, "@dup value");
                stack.push(value);
                stack.push(value);
            }
            case "setmimetype" -> {
                Object value = pop(stack, "@setMimeType value");
                context.setMimeType(String.valueOf(value));
            }
            case "paramget" -> {
                String defaultValue = String.valueOf(pop(stack, "@paramGet default value"));
                String name = String.valueOf(pop(stack, "@paramGet parameter name"));
                stack.push(context.queryParamOrDefault(name, defaultValue));
            }
            case "pparamget" -> {
                String defaultValue = String.valueOf(pop(stack, "@pparamGet default value"));
                String name = String.valueOf(pop(stack, "@pparamGet parameter name"));
                stack.push(context.persistentParamOrDefault(name, defaultValue));
            }
            case "pparamset" -> {
                String name = String.valueOf(pop(stack, "@pparamSet name"));
                String value = String.valueOf(pop(stack, "@pparamSet value"));
                context.setPersistentParam(name, value);
            }
            case "pparamdel" -> {
                String name = String.valueOf(pop(stack, "@pparamDel name"));
                context.deletePersistentParam(name);
            }
            case "tparamget" -> {
                String defaultValue = String.valueOf(pop(stack, "@tparamGet default value"));
                String name = String.valueOf(pop(stack, "@tparamGet parameter name"));
                stack.push(context.temporaryParamOrDefault(name, defaultValue));
            }
            case "tparamset" -> {
                String name = String.valueOf(pop(stack, "@tparamSet name"));
                String value = String.valueOf(pop(stack, "@tparamSet value"));
                context.setTemporaryParam(name, value);
            }
            case "tparamdel" -> {
                String name = String.valueOf(pop(stack, "@tparamDel name"));
                context.deleteTemporaryParam(name);
            }
            default -> throw new ScriptRuntimeException("Unsupported function: @" + functionName);
        }
    }

    private Object resolveTokenValue(ExpressionToken token) {
        return switch (token.type()) {
            case INTEGER -> Integer.parseInt(token.text());
            case DOUBLE -> Double.parseDouble(token.text());
            case STRING -> token.text();
            case VARIABLE -> multistack.peek(token.text()).getValue();
            case FUNCTION, OPERATOR -> throw new ScriptRuntimeException(
                    "FOR expressions cannot contain operators or functions.");
        };
    }

    private Object pop(Deque<Object> stack, String description) {
        if (stack.isEmpty()) {
            throw new ScriptRuntimeException("Missing value on stack for " + description + ".");
        }
        return stack.pop();
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
