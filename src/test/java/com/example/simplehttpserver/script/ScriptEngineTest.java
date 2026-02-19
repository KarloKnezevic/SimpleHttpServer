package com.example.simplehttpserver.script;

import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.server.Session;
import com.example.simplehttpserver.server.SessionManager;
import com.example.simplehttpserver.script.ast.ScriptDocumentNode;
import com.example.simplehttpserver.script.parser.ScriptParser;
import com.example.simplehttpserver.script.runtime.ScriptEngine;
import com.example.simplehttpserver.script.runtime.ScriptExecutionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptEngineTest {

    private final SessionManager sessionManager = new SessionManager(60);

    @AfterEach
    void tearDown() {
        sessionManager.close();
    }

    @Test
    void executesForLoopAndRpnEcho() {
        String source = "[$ FOR i 1 3 1 $][$= i $][$END$][$= 2 3 + $]";
        ScriptDocumentNode document = new ScriptParser().parse(source);

        Session session = sessionManager.resolve(dummyRequest()).session();
        ScriptExecutionContext context = new ScriptExecutionContext(Map.of(), session);

        new ScriptEngine().execute(document, context);

        assertEquals("1235", context.outputText());
    }

    @Test
    void supportsMimeTypeAndParameterFunctions() {
        String source = "[$= \"text/plain; charset=UTF-8\" @setMimeType $][$= \"name\" \"guest\" @paramGet $]";
        ScriptDocumentNode document = new ScriptParser().parse(source);

        Session session = sessionManager.resolve(dummyRequest()).session();
        ScriptExecutionContext context = new ScriptExecutionContext(Map.of("name", List.of("Alice")), session);

        new ScriptEngine().execute(document, context);

        assertEquals("text/plain; charset=UTF-8", context.mimeType());
        assertTrue(context.outputText().contains("Alice"));
    }

    private HttpRequest dummyRequest() {
        return new HttpRequest(
                HttpMethod.GET,
                "/",
                "/",
                "HTTP/1.1",
                Map.of(),
                Map.of(),
                new byte[0]
        );
    }
}
