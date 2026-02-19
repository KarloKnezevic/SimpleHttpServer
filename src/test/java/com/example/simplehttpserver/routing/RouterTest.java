package com.example.simplehttpserver.routing;

import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpStatus;
import com.example.simplehttpserver.server.Session;
import com.example.simplehttpserver.server.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterTest {

    private final SessionManager sessionManager = new SessionManager(60);

    @AfterEach
    void tearDown() {
        sessionManager.close();
    }

    @Test
    void matchesParameterizedRouteAndExtractsPathVariable() throws Exception {
        Router router = new Router();
        router.addRoute(HttpMethod.GET, "/users/{id}", context ->
                HttpResponse.text(HttpStatus.OK, context.pathParam("id").orElseThrow()));

        HttpRequest request = new HttpRequest(
                HttpMethod.GET,
                "/users/42",
                "/users/42",
                "HTTP/1.1",
                Map.of(),
                Map.of(),
                new byte[0]
        );

        Session session = sessionManager.resolve(request).session();
        HttpResponse response = router.dispatch(request, session).orElseThrow();

        assertEquals("42", new String(response.body()));
    }

    @Test
    void reportsAllowedMethodsForMatchingPath() {
        Router router = new Router();
        router.addRoute(HttpMethod.GET, "/hello", context -> HttpResponse.text(HttpStatus.OK, "hi"));
        router.addRoute(HttpMethod.POST, "/hello", context -> HttpResponse.text(HttpStatus.OK, "posted"));

        assertEquals(2, router.allowedMethods("/hello").size());
        assertTrue(router.allowedMethods("/missing").isEmpty());
    }
}
