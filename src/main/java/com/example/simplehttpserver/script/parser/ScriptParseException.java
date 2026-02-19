package com.example.simplehttpserver.script.parser;

/**
 * Exception thrown when script source cannot be parsed.
 */
public class ScriptParseException extends RuntimeException {

    public ScriptParseException(String message) {
        super(message);
    }

    public ScriptParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
