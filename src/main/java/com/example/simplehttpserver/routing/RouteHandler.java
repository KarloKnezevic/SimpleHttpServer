package com.example.simplehttpserver.routing;

import com.example.simplehttpserver.http.HttpResponse;

/**
 * Functional interface for route handlers.
 */
@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(RequestContext context) throws Exception;
}
