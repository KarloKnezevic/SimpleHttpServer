package com.example.simplehttpserver.script.lexer;

/**
 * Token types used inside a script tag.
 */
public enum TagTokenType {
    ECHO_MARKER,
    IDENTIFIER,
    INTEGER,
    DOUBLE,
    STRING,
    FUNCTION,
    OPERATOR
}
