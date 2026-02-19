package com.example.simplehttpserver.script.lexer;

import com.example.simplehttpserver.script.parser.ScriptParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits full script source into TEXT and TAG blocks.
 */
public class ScriptDocumentLexer {

    public List<DocumentToken> tokenize(String source) {
        if (source == null) {
            throw new ScriptParseException("Script source must not be null.");
        }

        List<DocumentToken> tokens = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int textStart = 0;

        int index = 0;
        while (index < source.length()) {
            if (isTagStart(source, index)) {
                if (!text.isEmpty()) {
                    tokens.add(new DocumentToken(DocumentTokenType.TEXT, text.toString(), textStart));
                    text.setLength(0);
                }

                int tagStart = index;
                int tagEnd = findTagEnd(source, index + 2);
                String tagContent = source.substring(index + 2, tagEnd);
                tokens.add(new DocumentToken(DocumentTokenType.TAG, tagContent, tagStart));
                index = tagEnd + 2;
                textStart = index;
                continue;
            }

            char current = source.charAt(index);
            if (current == '\\') {
                if (index + 1 >= source.length()) {
                    throw new ScriptParseException("Invalid escape sequence at end of text block.");
                }

                char escaped = source.charAt(index + 1);
                if (escaped != '[' && escaped != '\\') {
                    throw new ScriptParseException("Invalid text escape sequence: \\" + escaped);
                }

                text.append(escaped);
                index += 2;
                continue;
            }

            text.append(current);
            index++;
        }

        if (!text.isEmpty()) {
            tokens.add(new DocumentToken(DocumentTokenType.TEXT, text.toString(), textStart));
        }

        return tokens;
    }

    private boolean isTagStart(String source, int index) {
        return index + 1 < source.length()
                && source.charAt(index) == '['
                && source.charAt(index + 1) == '$';
    }

    private int findTagEnd(String source, int searchStart) {
        boolean inString = false;
        boolean escaped = false;

        for (int i = searchStart; i < source.length() - 1; i++) {
            char current = source.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (current == '\\') {
                    escaped = true;
                    continue;
                }
                if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == '$' && source.charAt(i + 1) == ']') {
                return i;
            }
        }

        throw new ScriptParseException("Tag opened but not closed with $].");
    }
}
