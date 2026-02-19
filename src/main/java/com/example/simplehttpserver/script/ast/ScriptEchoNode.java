package com.example.simplehttpserver.script.ast;

import java.util.List;

/**
 * ECHO tag node containing expression tokens in evaluation order.
 */
public final class ScriptEchoNode extends ScriptNode {

    private final List<ExpressionToken> tokens;

    public ScriptEchoNode(List<ExpressionToken> tokens) {
        this.tokens = List.copyOf(tokens);
    }

    public List<ExpressionToken> tokens() {
        return tokens;
    }
}
