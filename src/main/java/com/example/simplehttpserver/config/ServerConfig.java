package com.example.simplehttpserver.config;

import java.util.Map;

/**
 * Immutable server settings loaded from a properties file.
 */
public record ServerConfig(
        String host,
        int port,
        int workerThreads,
        int queueCapacity,
        int socketTimeoutMillis,
        int sessionTimeoutSeconds,
        int maxHeaderBytes,
        int maxBodyBytes,
        String publicResourcesRoot,
        String templatesRoot,
        Map<String, String> mimeTypes
) {
    public ServerConfig {
        mimeTypes = Map.copyOf(mimeTypes);
    }
}
