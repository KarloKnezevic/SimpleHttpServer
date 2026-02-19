package com.example.simplehttpserver.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable representation of an incoming HTTP request.
 */
public record HttpRequest(
        HttpMethod method,
        String rawTarget,
        String path,
        String version,
        Map<String, List<String>> headers,
        Map<String, List<String>> queryParameters,
        byte[] body
) {
    public HttpRequest {
        headers = copyOfStringListMap(headers);
        queryParameters = copyOfStringListMap(queryParameters);
        body = body.clone();
    }

    public Optional<String> firstHeader(String name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    public Optional<String> firstQueryValue(String key) {
        List<String> values = queryParameters.get(key);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    public String bodyAsString() {
        return bodyAsString(StandardCharsets.UTF_8);
    }

    public String bodyAsString(Charset charset) {
        return new String(body, charset);
    }

    private static Map<String, List<String>> copyOfStringListMap(Map<String, List<String>> source) {
        return source.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
    }
}
