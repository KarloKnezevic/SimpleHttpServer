package com.example.simplehttpserver.http;

/**
 * Exception thrown when an HTTP request cannot be parsed according to server rules.
 */
public class HttpParseException extends Exception {

    private final HttpStatus status;

    public HttpParseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
