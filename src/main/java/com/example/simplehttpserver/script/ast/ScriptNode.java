package com.example.simplehttpserver.script.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Base AST node for the educational script language.
 */
public abstract class ScriptNode {

    private final List<ScriptNode> children = new ArrayList<>();

    public void addChild(ScriptNode child) {
        children.add(child);
    }

    public List<ScriptNode> children() {
        return List.copyOf(children);
    }
}
