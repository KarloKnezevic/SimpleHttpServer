package com.example.simplehttpserver.server;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory HTTP session bound to a SID cookie.
 */
public final class Session {

    private final String id;
    private final ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<>();
    private volatile Instant expiresAt;

    Session(String id, Instant expiresAt) {
        this.id = id;
        this.expiresAt = expiresAt;
    }

    public String id() {
        return id;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    void touch(Instant newExpiry) {
        expiresAt = newExpiry;
    }

    boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public void put(String key, String value) {
        attributes.put(key, value);
    }

    public void remove(String key) {
        attributes.remove(key);
    }

    public Map<String, String> snapshot() {
        return Map.copyOf(attributes);
    }
}
