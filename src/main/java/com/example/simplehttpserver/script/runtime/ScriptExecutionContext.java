package com.example.simplehttpserver.script.runtime;

import com.example.simplehttpserver.server.Session;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Context object exposed to script engine functions.
 */
public class ScriptExecutionContext {

    private final Map<String, List<String>> queryParameters;
    private final Map<String, String> temporaryParameters = new HashMap<>();
    private final Session session;
    private final StringBuilder output = new StringBuilder();

    private String mimeType = "text/html; charset=UTF-8";

    public ScriptExecutionContext(Map<String, List<String>> queryParameters, Session session) {
        this.queryParameters = queryParameters;
        this.session = session;
    }

    public void write(String text) {
        output.append(text);
    }

    public String outputText() {
        return output.toString();
    }

    public byte[] outputBytes() {
        return output.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String mimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new ScriptRuntimeException("MIME type must not be blank.");
        }
        this.mimeType = mimeType;
    }

    public String queryParamOrDefault(String name, String defaultValue) {
        List<String> values = queryParameters.get(name);
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        return values.get(0);
    }

    public String persistentParamOrDefault(String name, String defaultValue) {
        return session.get(name).orElse(defaultValue);
    }

    public void setPersistentParam(String name, String value) {
        session.put(name, value);
    }

    public void deletePersistentParam(String name) {
        session.remove(name);
    }

    public String temporaryParamOrDefault(String name, String defaultValue) {
        return Optional.ofNullable(temporaryParameters.get(name)).orElse(defaultValue);
    }

    public void setTemporaryParam(String name, String value) {
        temporaryParameters.put(name, value);
    }

    public void deleteTemporaryParam(String name) {
        temporaryParameters.remove(name);
    }
}
