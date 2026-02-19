package com.example.simplehttpserver.server;

import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpStatus;
import com.example.simplehttpserver.script.ast.ScriptDocumentNode;
import com.example.simplehttpserver.script.parser.ScriptParseException;
import com.example.simplehttpserver.script.parser.ScriptParser;
import com.example.simplehttpserver.script.runtime.ScriptEngine;
import com.example.simplehttpserver.script.runtime.ScriptExecutionContext;
import com.example.simplehttpserver.script.runtime.ScriptRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Loads and executes .smscr scripts from classpath resources.
 */
public class ScriptService {

    private final String resourcesRoot;
    private final ScriptParser parser = new ScriptParser();

    public ScriptService(String resourcesRoot) {
        this.resourcesRoot = resourcesRoot;
    }

    public Optional<HttpResponse> tryExecute(String requestPath, HttpRequest request, Session session) {
        if (requestPath == null || !requestPath.endsWith(".smscr")) {
            return Optional.empty();
        }

        Path normalizedPath = normalizeRelativePath(requestPath);
        if (normalizedPath == null) {
            return Optional.of(HttpResponse.text(HttpStatus.FORBIDDEN,
                    "Path traversal attempt was blocked."));
        }

        String resourcePath = normalizedPath.toString().replace('\\', '/');
        String fullResourceName = resourcesRoot + "/" + resourcePath;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fullResourceName)) {
            if (in == null) {
                return Optional.empty();
            }

            String scriptSource = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            ScriptDocumentNode document = parser.parse(scriptSource);

            ScriptExecutionContext context = new ScriptExecutionContext(request.queryParameters(), session);
            new ScriptEngine().execute(document, context);

            HttpResponse response = HttpResponse.status(HttpStatus.OK)
                    .header("Content-Type", context.mimeType())
                    .body(context.outputBytes())
                    .build();

            return Optional.of(response);
        } catch (ScriptParseException parseException) {
            return Optional.of(HttpResponse.text(HttpStatus.BAD_REQUEST,
                    "Script parse error: " + parseException.getMessage()));
        } catch (ScriptRuntimeException runtimeException) {
            return Optional.of(HttpResponse.text(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Script execution error: " + runtimeException.getMessage()));
        } catch (IOException ioException) {
            return Optional.of(HttpResponse.text(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load script resource."));
        }
    }

    private Path normalizeRelativePath(String requestPath) {
        String relative = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
        Path normalized = Path.of(relative).normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            return null;
        }
        return normalized;
    }
}
