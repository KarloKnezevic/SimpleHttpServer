package com.example.simplehttpserver.script.lexer;

/**
 * Single token from script document lexing.
 *
 * @param type token kind
 * @param content token content (text body or raw tag content)
 * @param position starting character index in source
 */
public record DocumentToken(DocumentTokenType type, String content, int position) {
}
