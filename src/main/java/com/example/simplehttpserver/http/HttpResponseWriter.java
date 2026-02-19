package com.example.simplehttpserver.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializes {@link HttpResponse} objects to bytes on the client socket output stream.
 */
public class HttpResponseWriter {

    private static final DateTimeFormatter RFC_1123 = DateTimeFormatter.RFC_1123_DATE_TIME;

    public void write(OutputStream outputStream, HttpMethod requestMethod, HttpResponse response) throws IOException {
        byte[] body = response.body();

        Map<String, String> headers = new LinkedHashMap<>(response.headers());
        headers.putIfAbsent("Date", RFC_1123.format(ZonedDateTime.now(ZoneOffset.UTC)));
        headers.putIfAbsent("Server", "SimpleHttpServer/2.0");
        headers.putIfAbsent("Connection", "close");
        headers.putIfAbsent("Content-Length", Integer.toString(body.length));

        StringBuilder statusAndHeaders = new StringBuilder();
        statusAndHeaders
                .append("HTTP/1.1 ")
                .append(response.status().code())
                .append(' ')
                .append(response.status().reason())
                .append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            statusAndHeaders
                    .append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        statusAndHeaders.append("\r\n");

        outputStream.write(statusAndHeaders.toString().getBytes(StandardCharsets.ISO_8859_1));
        if (requestMethod != HttpMethod.HEAD) {
            outputStream.write(body);
        }
        outputStream.flush();
    }
}
