package com.example.simplehttpserver.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpRequestParserTest {

    private final HttpRequestParser parser = new HttpRequestParser();

    @Test
    void parsesRequestLineAndQueryParameters() throws Exception {
        String raw = "GET /echo?x=1&x=2&name=Alice HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "\r\n";

        HttpRequest request = parser.parse(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.ISO_8859_1)),
                4096,
                4096
        );

        assertEquals(HttpMethod.GET, request.method());
        assertEquals("/echo", request.path());
        assertEquals("HTTP/1.1", request.version());
        assertEquals("localhost", request.firstHeader("Host").orElseThrow());
        assertEquals("Alice", request.firstQueryValue("name").orElseThrow());
        assertEquals(2, request.queryParameters().get("x").size());
    }

    @Test
    void parsesHeadersAndBodyForPost() throws Exception {
        String body = "hello";
        String raw = "POST /submit HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: text/plain\r\n"
                + "Content-Length: 5\r\n"
                + "\r\n"
                + body;

        HttpRequest request = parser.parse(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.ISO_8859_1)),
                4096,
                4096
        );

        assertEquals(HttpMethod.POST, request.method());
        assertEquals("text/plain", request.firstHeader("Content-Type").orElseThrow());
        assertEquals("hello", request.bodyAsString());
    }

    @Test
    void rejectsInvalidRequestLine() {
        String raw = "GET /only-two-parts\r\nHost: localhost\r\n\r\n";

        HttpParseException exception = assertThrows(HttpParseException.class, () -> parser.parse(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.ISO_8859_1)),
                4096,
                4096
        ));

        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }
}
