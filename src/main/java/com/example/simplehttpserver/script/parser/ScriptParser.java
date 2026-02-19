package com.example.simplehttpserver.script.parser;

import com.example.simplehttpserver.script.ast.ExpressionToken;
import com.example.simplehttpserver.script.ast.ExpressionTokenType;
import com.example.simplehttpserver.script.ast.ScriptDocumentNode;
import com.example.simplehttpserver.script.ast.ScriptEchoNode;
import com.example.simplehttpserver.script.ast.ScriptForLoopNode;
import com.example.simplehttpserver.script.ast.ScriptNode;
import com.example.simplehttpserver.script.ast.ScriptTextBlockNode;
import com.example.simplehttpserver.script.lexer.DocumentToken;
import com.example.simplehttpserver.script.lexer.DocumentTokenType;
import com.example.simplehttpserver.script.lexer.ScriptDocumentLexer;
import com.example.simplehttpserver.script.lexer.TagLexer;
import com.example.simplehttpserver.script.lexer.TagToken;
import com.example.simplehttpserver.script.lexer.TagTokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Parses script source into an AST.
 */
public class ScriptParser {

    private final ScriptDocumentLexer documentLexer = new ScriptDocumentLexer();
    private final TagLexer tagLexer = new TagLexer();

    public ScriptDocumentNode parse(String scriptSource) {
        List<DocumentToken> documentTokens = documentLexer.tokenize(scriptSource);

        ScriptDocumentNode root = new ScriptDocumentNode();
        Deque<ScriptNode> nodeStack = new ArrayDeque<>();
        nodeStack.push(root);

        for (DocumentToken token : documentTokens) {
            if (token.type() == DocumentTokenType.TEXT) {
                nodeStack.peek().addChild(new ScriptTextBlockNode(token.content()));
                continue;
            }

            ParsedTag parsedTag = parseTag(token.content());
            if (parsedTag.endTag()) {
                if (nodeStack.size() == 1) {
                    throw new ScriptParseException("END tag without matching FOR block.");
                }
                nodeStack.pop();
                continue;
            }

            ScriptNode node = parsedTag.node();
            nodeStack.peek().addChild(node);

            if (node instanceof ScriptForLoopNode) {
                nodeStack.push(node);
            }
        }

        if (nodeStack.size() != 1) {
            throw new ScriptParseException("Unclosed FOR block: missing END tag.");
        }

        return root;
    }

    private ParsedTag parseTag(String rawTagBody) {
        List<TagToken> tagTokens = tagLexer.tokenize(rawTagBody);
        if (tagTokens.isEmpty()) {
            throw new ScriptParseException("Empty tag is not allowed.");
        }

        TagToken first = tagTokens.get(0);

        if (first.type() == TagTokenType.ECHO_MARKER) {
            List<ExpressionToken> expressionTokens = parseEchoTokens(tagTokens.subList(1, tagTokens.size()));
            return ParsedTag.node(new ScriptEchoNode(expressionTokens));
        }

        if (first.type() != TagTokenType.IDENTIFIER) {
            throw new ScriptParseException("Tag must start with keyword FOR/END or with '=' for echo tags.");
        }

        String keyword = first.lexeme().toUpperCase(Locale.ROOT);
        return switch (keyword) {
            case "FOR" -> ParsedTag.node(parseForTag(tagTokens));
            case "END" -> parseEndTag(tagTokens);
            default -> throw new ScriptParseException("Unsupported tag keyword: " + first.lexeme());
        };
    }

    private ScriptForLoopNode parseForTag(List<TagToken> tagTokens) {
        if (tagTokens.size() != 4 && tagTokens.size() != 5) {
            throw new ScriptParseException("FOR tag expects 3 or 4 arguments after variable name.");
        }

        TagToken variableToken = tagTokens.get(1);
        if (variableToken.type() != TagTokenType.IDENTIFIER) {
            throw new ScriptParseException("FOR loop variable must be an identifier.");
        }

        ExpressionToken start = parseForExpression(tagTokens.get(2));
        ExpressionToken end = parseForExpression(tagTokens.get(3));
        ExpressionToken step = tagTokens.size() == 5
                ? parseForExpression(tagTokens.get(4))
                : new ExpressionToken(ExpressionTokenType.INTEGER, "1");

        return new ScriptForLoopNode(variableToken.lexeme(), start, end, step);
    }

    private ParsedTag parseEndTag(List<TagToken> tagTokens) {
        if (tagTokens.size() != 1) {
            throw new ScriptParseException("END tag does not accept arguments.");
        }
        return ParsedTag.end();
    }

    private List<ExpressionToken> parseEchoTokens(List<TagToken> tagTokens) {
        List<ExpressionToken> result = new ArrayList<>();
        for (TagToken tagToken : tagTokens) {
            result.add(toExpressionToken(tagToken));
        }
        return result;
    }

    private ExpressionToken parseForExpression(TagToken tagToken) {
        ExpressionToken expressionToken = toExpressionToken(tagToken);
        if (expressionToken.type() == ExpressionTokenType.OPERATOR
                || expressionToken.type() == ExpressionTokenType.FUNCTION) {
            throw new ScriptParseException("FOR expression must be a literal or variable.");
        }
        return expressionToken;
    }

    private ExpressionToken toExpressionToken(TagToken token) {
        return switch (token.type()) {
            case IDENTIFIER -> new ExpressionToken(ExpressionTokenType.VARIABLE, token.lexeme());
            case INTEGER -> new ExpressionToken(ExpressionTokenType.INTEGER, token.lexeme());
            case DOUBLE -> new ExpressionToken(ExpressionTokenType.DOUBLE, token.lexeme());
            case STRING -> new ExpressionToken(ExpressionTokenType.STRING, token.lexeme());
            case FUNCTION -> new ExpressionToken(ExpressionTokenType.FUNCTION, token.lexeme());
            case OPERATOR -> new ExpressionToken(ExpressionTokenType.OPERATOR, token.lexeme());
            case ECHO_MARKER -> throw new ScriptParseException("Unexpected '=' token in expression stream.");
        };
    }

    private record ParsedTag(ScriptNode node, boolean endTag) {

        static ParsedTag node(ScriptNode node) {
            return new ParsedTag(node, false);
        }

        static ParsedTag end() {
            return new ParsedTag(null, true);
        }
    }
}
