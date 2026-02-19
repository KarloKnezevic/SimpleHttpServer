package com.example.simplehttpserver.script.runtime;

/**
 * Exception thrown when a parsed script fails during evaluation.
 */
public class ScriptRuntimeException extends RuntimeException {

    public ScriptRuntimeException(String message) {
        super(message);
    }

    public ScriptRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
