package com.example.simplehttpserver.script.ast;

/**
 * FOR loop node.
 */
public final class ScriptForLoopNode extends ScriptNode {

    private final String variableName;
    private final ExpressionToken startExpression;
    private final ExpressionToken endExpression;
    private final ExpressionToken stepExpression;

    public ScriptForLoopNode(
            String variableName,
            ExpressionToken startExpression,
            ExpressionToken endExpression,
            ExpressionToken stepExpression
    ) {
        this.variableName = variableName;
        this.startExpression = startExpression;
        this.endExpression = endExpression;
        this.stepExpression = stepExpression;
    }

    public String variableName() {
        return variableName;
    }

    public ExpressionToken startExpression() {
        return startExpression;
    }

    public ExpressionToken endExpression() {
        return endExpression;
    }

    public ExpressionToken stepExpression() {
        return stepExpression;
    }
}
