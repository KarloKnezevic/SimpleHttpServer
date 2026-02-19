package com.example.simplehttpserver.server;

import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Serves static files from a classpath resource root, with path traversal protection.
 */
public class StaticFileService {

    private final String resourcesRoot;
    private final Map<String, String> mimeTypes;

    public StaticFileService(String resourcesRoot, Map<String, String> mimeTypes) {
        this.resourcesRoot = resourcesRoot;
        this.mimeTypes = mimeTypes;
    }

    public Optional<HttpResponse> tryServe(String requestPath) {
        String path = requestPath;
        if (path == null || path.isBlank() || "/".equals(path)) {
            path = "/index.html";
        }

        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        Path normalizedPath = Path.of(relativePath).normalize();

        if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
            return Optional.of(HttpResponse.text(HttpStatus.FORBIDDEN,
                    "Path traversal attempt was blocked."));
        }

        String normalizedResource = normalizedPath.toString().replace('\\', '/');
        String fullResourceName = resourcesRoot + "/" + normalizedResource;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fullResourceName)) {
            if (in == null) {
                return Optional.empty();
            }

            byte[] content = in.readAllBytes();
            String contentType = contentTypeFromPath(normalizedResource);

            return Optional.of(HttpResponse.status(HttpStatus.OK)
                    .header("Content-Type", contentType)
                    .body(content)
                    .build());
        } catch (IOException exception) {
            return Optional.of(HttpResponse.text(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read static resource."));
        }
    }

    private String contentTypeFromPath(String path) {
        int extensionIndex = path.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == path.length() - 1) {
            return "application/octet-stream";
        }

        String extension = path.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }
}
