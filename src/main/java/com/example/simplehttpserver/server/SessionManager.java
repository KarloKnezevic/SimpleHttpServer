package com.example.simplehttpserver.server;

import com.example.simplehttpserver.http.HttpRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages in-memory sessions and session cookie generation.
 */
public final class SessionManager implements AutoCloseable {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final int timeoutSeconds;

    public SessionManager(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        cleanupExecutor.scheduleAtFixedRate(this::removeExpiredSessions, timeoutSeconds, timeoutSeconds, TimeUnit.SECONDS);
    }

    public SessionResolution resolve(HttpRequest request) {
        Instant now = Instant.now();
        String sessionId = findSessionId(request).orElse(null);

        if (sessionId != null) {
            Session existingSession = sessions.get(sessionId);
            if (existingSession != null && !existingSession.isExpired(now)) {
                existingSession.touch(now.plusSeconds(timeoutSeconds));
                return new SessionResolution(existingSession, Optional.empty());
            }
            sessions.remove(sessionId);
        }

        Session newSession = new Session(generateSessionId(), now.plusSeconds(timeoutSeconds));
        sessions.put(newSession.id(), newSession);
        String setCookieHeader = "SID=" + newSession.id() + "; Path=/; HttpOnly";
        return new SessionResolution(newSession, Optional.of(setCookieHeader));
    }

    private Optional<String> findSessionId(HttpRequest request) {
        return request.firstHeader("Cookie")
                .flatMap(cookieHeader -> {
                    String[] cookieParts = cookieHeader.split(";");
                    for (String part : cookieParts) {
                        String trimmed = part.trim();
                        if (trimmed.startsWith("SID=")) {
                            return Optional.of(trimmed.substring("SID=".length()));
                        }
                    }
                    return Optional.empty();
                });
    }

    private String generateSessionId() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void removeExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
        sessions.clear();
    }

    public record SessionResolution(Session session, Optional<String> setCookieHeader) {
    }
}
