package com.example.simplehttpserver.script;

import com.example.simplehttpserver.script.lexer.DocumentToken;
import com.example.simplehttpserver.script.lexer.DocumentTokenType;
import com.example.simplehttpserver.script.lexer.ScriptDocumentLexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScriptDocumentLexerTest {

    @Test
    void splitsTextAndTagBlocks() {
        String source = "Hello [$= 1 2 + $] world";

        List<DocumentToken> tokens = new ScriptDocumentLexer().tokenize(source);

        assertEquals(3, tokens.size());
        assertEquals(DocumentTokenType.TEXT, tokens.get(0).type());
        assertEquals("Hello ", tokens.get(0).content());
        assertEquals(DocumentTokenType.TAG, tokens.get(1).type());
        assertEquals("= 1 2 + ", tokens.get(1).content());
        assertEquals(DocumentTokenType.TEXT, tokens.get(2).type());
        assertEquals(" world", tokens.get(2).content());
    }
}
