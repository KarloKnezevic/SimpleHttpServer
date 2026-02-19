package com.example.simplehttpserver.routing;

import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.server.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple educational router that supports exact and parameterized paths such as /users/{id}.
 */
public class Router {

    private final List<Route> routes = new ArrayList<>();

    public Router addRoute(HttpMethod method, String pathPattern, RouteHandler handler) {
        routes.add(new Route(method, parsePattern(pathPattern), handler));
        return this;
    }

    public Optional<HttpResponse> dispatch(HttpRequest request, Session session) throws Exception {
        for (Route route : routes) {
            if (route.method != request.method()) {
                continue;
            }

            Optional<Map<String, String>> match = match(route.patternSegments, request.path());
            if (match.isEmpty()) {
                continue;
            }

            RequestContext context = new RequestContext(request, match.get(), session);
            return Optional.of(route.handler.handle(context));
        }
        return Optional.empty();
    }

    public Set<HttpMethod> allowedMethods(String path) {
        Set<HttpMethod> methods = new LinkedHashSet<>();
        for (Route route : routes) {
            if (match(route.patternSegments, path).isPresent()) {
                methods.add(route.method);
            }
        }
        return methods;
    }

    private List<String> parsePattern(String pattern) {
        if (pattern == null || pattern.isBlank() || !pattern.startsWith("/")) {
            throw new IllegalArgumentException("Route pattern must start with '/': " + pattern);
        }

        if ("/".equals(pattern)) {
            return List.of();
        }

        return splitPath(pattern);
    }

    private Optional<Map<String, String>> match(List<String> patternSegments, String path) {
        List<String> pathSegments = splitPath(path);
        if (patternSegments.size() != pathSegments.size()) {
            return Optional.empty();
        }

        Map<String, String> pathParameters = new LinkedHashMap<>();
        for (int i = 0; i < patternSegments.size(); i++) {
            String patternSegment = patternSegments.get(i);
            String actualSegment = pathSegments.get(i);

            if (isPathVariable(patternSegment)) {
                String variableName = patternSegment.substring(1, patternSegment.length() - 1);
                pathParameters.put(variableName, actualSegment);
                continue;
            }

            if (!patternSegment.equals(actualSegment)) {
                return Optional.empty();
            }
        }

        return Optional.of(pathParameters);
    }

    private boolean isPathVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2;
    }

    private List<String> splitPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return List.of();
        }

        String normalized = path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isBlank()) {
            return List.of();
        }

        return List.of(normalized.split("/"));
    }

    public String describeRoutes() {
        return routes.stream()
                .map(route -> route.method + " /" + String.join("/", route.patternSegments))
                .collect(Collectors.joining("\n"));
    }

    private record Route(HttpMethod method, List<String> patternSegments, RouteHandler handler) {
    }
}
