package com.example.simplehttpserver.script;

import com.example.simplehttpserver.script.ast.ScriptDocumentNode;
import com.example.simplehttpserver.script.ast.ExpressionTokenType;
import com.example.simplehttpserver.script.ast.ScriptForLoopNode;
import com.example.simplehttpserver.script.parser.ScriptParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ScriptParserTest {

    @Test
    void parsesForBlockAndEchoNode() {
        String source = "[$ FOR i 1 3 1 $]A[$= i $][$END$]";

        ScriptDocumentNode document = new ScriptParser().parse(source);

        assertEquals(1, document.children().size());
        ScriptForLoopNode forLoopNode = assertInstanceOf(ScriptForLoopNode.class, document.children().get(0));
        assertEquals("i", forLoopNode.variableName());
        assertEquals(ExpressionTokenType.INTEGER, forLoopNode.startExpression().type());
        assertEquals(ExpressionTokenType.INTEGER, forLoopNode.endExpression().type());
        assertEquals(ExpressionTokenType.INTEGER, forLoopNode.stepExpression().type());
        assertEquals(2, forLoopNode.children().size());
    }
}
