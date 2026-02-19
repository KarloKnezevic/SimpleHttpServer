package com.example.simplehttpserver.http;

import java.util.Arrays;

/**
 * Supported HTTP methods for this educational server.
 */
public enum HttpMethod {
    GET,
    POST,
    HEAD;

    public static HttpMethod fromToken(String token) throws HttpParseException {
        return Arrays.stream(values())
                .filter(method -> method.name().equals(token))
                .findFirst()
                .orElseThrow(() -> new HttpParseException(HttpStatus.NOT_IMPLEMENTED,
                        "Unsupported method: " + token));
    }
}
