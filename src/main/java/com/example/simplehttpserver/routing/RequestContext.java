package com.example.simplehttpserver.routing;

import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.server.Session;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data passed to a route handler: request details, path parameters and session.
 */
public final class RequestContext {

    private final HttpRequest request;
    private final Map<String, String> pathParameters;
    private final Session session;

    public RequestContext(HttpRequest request, Map<String, String> pathParameters, Session session) {
        this.request = request;
        this.pathParameters = Map.copyOf(pathParameters);
        this.session = session;
    }

    public HttpRequest request() {
        return request;
    }

    public Session session() {
        return session;
    }

    public Optional<String> pathParam(String name) {
        return Optional.ofNullable(pathParameters.get(name));
    }

    public Map<String, String> pathParams() {
        return pathParameters;
    }

    public Optional<String> queryParam(String name) {
        return request.firstQueryValue(name);
    }

    public Map<String, List<String>> queryParams() {
        return request.queryParameters();
    }
}
