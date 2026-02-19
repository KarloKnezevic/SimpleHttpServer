package com.example.simplehttpserver.script.ast;

/**
 * Token in expression order (RPN for ECHO tags).
 *
 * @param type expression token type
 * @param text token text or value
 */
public record ExpressionToken(ExpressionTokenType type, String text) {
}
