package com.example.simplehttpserver.script.lexer;

/**
 * Single token inside a script tag.
 *
 * @param type token type
 * @param lexeme normalized token text or unescaped string value
 */
public record TagToken(TagTokenType type, String lexeme) {
}
