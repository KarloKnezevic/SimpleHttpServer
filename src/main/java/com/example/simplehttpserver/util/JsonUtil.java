package com.example.simplehttpserver.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Small JSON utility used to avoid external dependencies in this educational project.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toQueryJson(Map<String, List<String>> queryParameters) {
        StringBuilder json = new StringBuilder("{");

        Iterator<Map.Entry<String, List<String>>> iterator = queryParameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            json.append('"').append(escape(entry.getKey())).append('"').append(':');

            List<String> values = entry.getValue();
            if (values.size() == 1) {
                json.append('"').append(escape(values.get(0))).append('"');
            } else {
                json.append('[');
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) {
                        json.append(',');
                    }
                    json.append('"').append(escape(values.get(i))).append('"');
                }
                json.append(']');
            }

            if (iterator.hasNext()) {
                json.append(',');
            }
        }

        return json.append('}').toString();
    }

    public static String escape(String text) {
        StringBuilder escaped = new StringBuilder();
        for (char character : text.toCharArray()) {
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
