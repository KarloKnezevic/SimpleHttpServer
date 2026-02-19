package com.example.simplehttpserver.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Parses raw bytes from a socket into a structured {@link HttpRequest}.
 */
public class HttpRequestParser {

    public HttpRequest parse(InputStream inputStream, int maxHeaderBytes, int maxBodyBytes)
            throws IOException, HttpParseException {
        byte[] headerBytes = readHeaderBlock(inputStream, maxHeaderBytes);
        List<String> lines = toLines(headerBytes);

        if (lines.isEmpty()) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST, "Request header is empty.");
        }

        String requestLine = lines.get(0);
        String[] requestParts = requestLine.trim().split("\\s+");
        if (requestParts.length != 3) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST, "Invalid request line: " + requestLine);
        }

        HttpMethod method = HttpMethod.fromToken(requestParts[0]);
        String rawTarget = requestParts[1];
        String version = requestParts[2];

        if (!"HTTP/1.0".equals(version) && !"HTTP/1.1".equals(version)) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST, "Unsupported HTTP version: " + version);
        }

        RequestTarget requestTarget = parseTarget(rawTarget);
        Map<String, List<String>> headers = parseHeaders(lines.subList(1, lines.size()));

        if (containsChunkedEncoding(headers)) {
            throw new HttpParseException(HttpStatus.NOT_IMPLEMENTED,
                    "Chunked transfer encoding is not implemented in this educational server.");
        }

        int contentLength = resolveContentLength(headers);
        if (contentLength > maxBodyBytes) {
            throw new HttpParseException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Request body exceeds configured maximum size.");
        }

        byte[] body = readBody(inputStream, contentLength);

        return new HttpRequest(
                method,
                rawTarget,
                requestTarget.path(),
                version,
                headers,
                requestTarget.queryParameters(),
                body
        );
    }

    private byte[] readHeaderBlock(InputStream inputStream, int maxHeaderBytes)
            throws IOException, HttpParseException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        int thirdLast = -1;
        int secondLast = -1;
        int last = -1;
        int headerLength = -1;

        while ((b = inputStream.read()) != -1) {
            buffer.write(b);

            if (buffer.size() > maxHeaderBytes) {
                throw new HttpParseException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "Request header exceeds configured maximum size.");
            }

            if (thirdLast == '\r' && secondLast == '\n' && last == '\r' && b == '\n') {
                headerLength = buffer.size() - 4;
                break;
            }

            if (last == '\n' && b == '\n') {
                headerLength = buffer.size() - 2;
                break;
            }

            thirdLast = secondLast;
            secondLast = last;
            last = b;
        }

        if (headerLength < 0) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Request header does not end with an empty line.");
        }

        return Arrays.copyOf(buffer.toByteArray(), headerLength);
    }

    private List<String> toLines(byte[] headerBytes) throws IOException {
        if (headerBytes.length == 0) {
            return Collections.emptyList();
        }

        String text = new String(headerBytes, StandardCharsets.ISO_8859_1);
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

    private Map<String, List<String>> parseHeaders(List<String> headerLines) throws HttpParseException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (String line : headerLines) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new HttpParseException(HttpStatus.BAD_REQUEST, "Invalid header line: " + line);
            }

            String name = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();

            headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        }

        return headers;
    }

    private boolean containsChunkedEncoding(Map<String, List<String>> headers) {
        List<String> values = headers.get("Transfer-Encoding");
        if (values == null) {
            return false;
        }

        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains("chunked"));
    }

    private int resolveContentLength(Map<String, List<String>> headers) throws HttpParseException {
        List<String> values = headers.get("Content-Length");
        if (values == null || values.isEmpty()) {
            return 0;
        }

        String first = values.get(0).trim();
        for (String value : values) {
            if (!first.equals(value.trim())) {
                throw new HttpParseException(HttpStatus.BAD_REQUEST,
                        "Multiple different Content-Length values are not allowed.");
            }
        }

        try {
            int contentLength = Integer.parseInt(first);
            if (contentLength < 0) {
                throw new HttpParseException(HttpStatus.BAD_REQUEST,
                        "Content-Length cannot be negative.");
            }
            return contentLength;
        } catch (NumberFormatException exception) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Invalid Content-Length: " + first);
        }
    }

    private byte[] readBody(InputStream inputStream, int contentLength) throws IOException, HttpParseException {
        if (contentLength == 0) {
            return new byte[0];
        }

        byte[] body = inputStream.readNBytes(contentLength);
        if (body.length != contentLength) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Request body ended before Content-Length bytes were received.");
        }

        return body;
    }

    private RequestTarget parseTarget(String rawTarget) throws HttpParseException {
        if (rawTarget == null || rawTarget.isBlank()) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST, "Request target is empty.");
        }

        if (!rawTarget.startsWith("/")) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Only origin-form request targets are supported.");
        }

        int querySeparator = rawTarget.indexOf('?');
        String rawPath = querySeparator >= 0 ? rawTarget.substring(0, querySeparator) : rawTarget;
        String rawQuery = querySeparator >= 0 ? rawTarget.substring(querySeparator + 1) : "";

        String decodedPath = decodePath(rawPath);
        if (decodedPath.isBlank()) {
            decodedPath = "/";
        }

        Map<String, List<String>> queryParameters = parseQuery(rawQuery);
        return new RequestTarget(decodedPath, queryParameters);
    }

    private String decodePath(String rawPath) throws HttpParseException {
        try {
            return URLDecoder.decode(rawPath.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Invalid URL-encoding in path: " + rawPath);
        }
    }

    private Map<String, List<String>> parseQuery(String rawQuery) throws HttpParseException {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }

        Map<String, List<String>> queryParameters = new LinkedHashMap<>();
        String[] pairs = rawQuery.split("&");

        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] keyValue = pair.split("=", 2);
            String key = decodeQueryPart(keyValue[0]);
            String value = keyValue.length == 2 ? decodeQueryPart(keyValue[1]) : "";

            queryParameters.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
        }

        return queryParameters;
    }

    private String decodeQueryPart(String text) throws HttpParseException {
        try {
            return URLDecoder.decode(text, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new HttpParseException(HttpStatus.BAD_REQUEST,
                    "Invalid URL-encoding in query string: " + text);
        }
    }

    private record RequestTarget(String path, Map<String, List<String>> queryParameters) {
    }
}
