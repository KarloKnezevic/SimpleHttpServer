package com.example.simplehttpserver.script.ast;

/**
 * Text node implementation used inside the AST tree.
 */
public final class ScriptTextBlockNode extends ScriptNode {

    private final String text;

    public ScriptTextBlockNode(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
