package com.example.simplehttpserver.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable HTTP response object with helper factory methods for common payload types.
 */
public final class HttpResponse {

    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(HttpStatus status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = Map.copyOf(headers);
        this.body = body.clone();
    }

    public static Builder status(HttpStatus status) {
        return new Builder(status);
    }

    public static HttpResponse text(HttpStatus status, String body) {
        return status(status)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(body)
                .build();
    }

    public static HttpResponse html(HttpStatus status, String body) {
        return status(status)
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(body)
                .build();
    }

    public static HttpResponse json(HttpStatus status, String body) {
        return status(status)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(body)
                .build();
    }

    public HttpResponse withHeader(String name, String value) {
        Map<String, String> updatedHeaders = new LinkedHashMap<>(headers);
        updatedHeaders.put(name, value);
        return new HttpResponse(status, updatedHeaders, body);
    }

    public HttpStatus status() {
        return status;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    public static final class Builder {

        private final HttpStatus status;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        private Builder(HttpStatus status) {
            this.status = status;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String value) {
            body = value.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public Builder body(byte[] value) {
            body = value.clone();
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(status, headers, body);
        }
    }
}
