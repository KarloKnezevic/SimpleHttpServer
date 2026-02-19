package com.example.simplehttpserver.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads server configuration from classpath defaults and optional external override file.
 */
public final class ServerConfigLoader {

    private ServerConfigLoader() {
    }

    public static ServerConfig load(Path externalConfigPath) throws IOException {
        Properties properties = new Properties();

        try (InputStream in = getRequiredResource("server.properties")) {
            properties.load(in);
        }

        if (externalConfigPath != null) {
            try (InputStream in = Files.newInputStream(externalConfigPath)) {
                properties.load(in);
            }
        }

        Map<String, String> mimeTypes = loadMimeTypes(properties.getProperty("server.mimeTypesResource", "mime-types.properties"));

        return new ServerConfig(
                properties.getProperty("server.host", "127.0.0.1"),
                parseInt(properties, "server.port", 8080),
                parseInt(properties, "server.workerThreads", 8),
                parseInt(properties, "server.queueCapacity", 64),
                parseInt(properties, "server.socketTimeoutMillis", 5000),
                parseInt(properties, "session.timeoutSeconds", 600),
                parseInt(properties, "server.maxHeaderBytes", 16384),
                parseInt(properties, "server.maxBodyBytes", 1048576),
                properties.getProperty("server.publicResourcesRoot", "public"),
                properties.getProperty("server.templatesRoot", "templates"),
                mimeTypes
        );
    }

    private static Map<String, String> loadMimeTypes(String resourceName) throws IOException {
        Properties mimeProperties = new Properties();
        try (InputStream in = getRequiredResource(resourceName)) {
            mimeProperties.load(in);
        }

        Map<String, String> mimeTypes = new LinkedHashMap<>();
        for (String key : mimeProperties.stringPropertyNames()) {
            mimeTypes.put(key.toLowerCase(), mimeProperties.getProperty(key));
        }
        return mimeTypes;
    }

    private static int parseInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static InputStream getRequiredResource(String resourceName) {
        InputStream in = ServerConfigLoader.class.getClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            throw new IllegalArgumentException("Missing required resource: " + resourceName);
        }
        return in;
    }
}
