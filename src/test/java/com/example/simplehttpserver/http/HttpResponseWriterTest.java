package com.example.simplehttpserver.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseWriterTest {

    @Test
    void serializesResponseWithStatusHeadersAndBody() throws Exception {
        HttpResponse response = HttpResponse.text(HttpStatus.OK, "Hello");
        HttpResponseWriter writer = new HttpResponseWriter();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.write(output, HttpMethod.GET, response);

        String serialized = output.toString(StandardCharsets.ISO_8859_1);

        assertTrue(serialized.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(serialized.contains("Content-Type: text/plain; charset=UTF-8\r\n"));
        assertTrue(serialized.contains("Content-Length: 5\r\n"));
        assertTrue(serialized.endsWith("\r\n\r\nHello"));
    }

    @Test
    void omitsBodyForHeadRequests() throws Exception {
        HttpResponse response = HttpResponse.text(HttpStatus.OK, "Hello");
        HttpResponseWriter writer = new HttpResponseWriter();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.write(output, HttpMethod.HEAD, response);

        String serialized = output.toString(StandardCharsets.ISO_8859_1);
        assertTrue(serialized.endsWith("\r\n\r\n"));
    }
}
