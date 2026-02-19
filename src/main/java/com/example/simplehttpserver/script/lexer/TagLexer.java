package com.example.simplehttpserver.script.lexer;

import com.example.simplehttpserver.script.parser.ScriptParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexes a single raw tag body into structured tag tokens.
 */
public class TagLexer {

    public List<TagToken> tokenize(String rawTagBody) {
        if (rawTagBody == null) {
            throw new ScriptParseException("Tag body must not be null.");
        }

        String input = rawTagBody.trim();
        List<TagToken> tokens = new ArrayList<>();

        int index = 0;
        if (!input.isEmpty() && input.charAt(0) == '=') {
            tokens.add(new TagToken(TagTokenType.ECHO_MARKER, "="));
            index = 1;
        }

        while (index < input.length()) {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
            if (index >= input.length()) {
                break;
            }

            char current = input.charAt(index);

            if (current == '"') {
                ParseResult stringResult = parseString(input, index);
                tokens.add(new TagToken(TagTokenType.STRING, stringResult.lexeme()));
                index = stringResult.nextIndex();
                continue;
            }

            if (current == '@') {
                ParseResult functionResult = parseFunction(input, index);
                tokens.add(new TagToken(TagTokenType.FUNCTION, functionResult.lexeme()));
                index = functionResult.nextIndex();
                continue;
            }

            ParseResult numberResult = parseNumber(input, index);
            if (numberResult != null) {
                TagTokenType type = numberResult.lexeme().contains(".") ? TagTokenType.DOUBLE : TagTokenType.INTEGER;
                tokens.add(new TagToken(type, numberResult.lexeme()));
                index = numberResult.nextIndex();
                continue;
            }

            if (isOperator(current)) {
                tokens.add(new TagToken(TagTokenType.OPERATOR, Character.toString(current)));
                index++;
                continue;
            }

            if (Character.isLetter(current)) {
                ParseResult identifierResult = parseIdentifier(input, index);
                tokens.add(new TagToken(TagTokenType.IDENTIFIER, identifierResult.lexeme()));
                index = identifierResult.nextIndex();
                continue;
            }

            throw new ScriptParseException("Unsupported character in tag: '" + current + "'.");
        }

        return tokens;
    }

    private ParseResult parseString(String input, int start) {
        StringBuilder value = new StringBuilder();

        int index = start + 1;
        while (index < input.length()) {
            char current = input.charAt(index);

            if (current == '\\') {
                if (index + 1 >= input.length()) {
                    throw new ScriptParseException("Unfinished escape sequence in string literal.");
                }

                char escaped = input.charAt(index + 1);
                switch (escaped) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'r' -> value.append('\r');
                    case '"' -> value.append('"');
                    case '\\' -> value.append('\\');
                    default -> throw new ScriptParseException("Unsupported string escape: \\" + escaped);
                }

                index += 2;
                continue;
            }

            if (current == '"') {
                return new ParseResult(value.toString(), index + 1);
            }

            value.append(current);
            index++;
        }

        throw new ScriptParseException("Unterminated string literal in tag.");
    }

    private ParseResult parseFunction(String input, int start) {
        int index = start + 1;
        if (index >= input.length() || !Character.isLetter(input.charAt(index))) {
            throw new ScriptParseException("Function name must start with a letter after '@'.");
        }

        while (index < input.length()) {
            char current = input.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '_') {
                break;
            }
            index++;
        }

        return new ParseResult(input.substring(start, index), index);
    }

    private ParseResult parseNumber(String input, int start) {
        int index = start;

        char first = input.charAt(index);
        if (first == '+' || first == '-') {
            if (index + 1 >= input.length()) {
                return null;
            }
            char next = input.charAt(index + 1);
            if (!Character.isDigit(next) && next != '.') {
                return null;
            }
            index++;
        }

        int integerStart = index;
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }

        boolean hasIntegerPart = index > integerStart;
        boolean hasDot = false;

        if (index < input.length() && input.charAt(index) == '.') {
            hasDot = true;
            index++;
            int decimalStart = index;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
            boolean hasDecimalPart = index > decimalStart;
            if (!hasIntegerPart && !hasDecimalPart) {
                return null;
            }
        }

        if (!hasIntegerPart && !hasDot) {
            return null;
        }

        if (index < input.length()) {
            char boundary = input.charAt(index);
            if (!Character.isWhitespace(boundary) && !isOperator(boundary)) {
                return null;
            }
        }

        return new ParseResult(input.substring(start, index), index);
    }

    private ParseResult parseIdentifier(String input, int start) {
        int index = start;
        while (index < input.length()) {
            char current = input.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '_') {
                break;
            }
            index++;
        }

        return new ParseResult(input.substring(start, index), index);
    }

    private boolean isOperator(char current) {
        return current == '+' || current == '-' || current == '*' || current == '/';
    }

    private record ParseResult(String lexeme, int nextIndex) {
    }
}
