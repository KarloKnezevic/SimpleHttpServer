package com.example.simplehttpserver.server;

import com.example.simplehttpserver.app.DefaultRoutes;
import com.example.simplehttpserver.config.ServerConfig;
import com.example.simplehttpserver.routing.Router;
import com.example.simplehttpserver.template.TemplateService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleHttpServerIntegrationTest {

    @Test
    void servesHelloRoute() throws Exception {
        ServerConfig config = testConfig();
        Router router = testRouter();

        try (SimpleHttpServer server = new SimpleHttpServer(config, router)) {
            server.start();
            URI uri = URI.create("http://127.0.0.1:" + server.getBoundPort() + "/hello");

            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals("Hello World", response.body());
        }
    }

    @Test
    void executesScriptFromSmscrResource() throws Exception {
        ServerConfig config = testConfig();
        Router router = testRouter();

        try (SimpleHttpServer server = new SimpleHttpServer(config, router)) {
            server.start();
            URI uri = URI.create("http://127.0.0.1:" + server.getBoundPort() + "/scripts/basic.smscr");

            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("Iteration 1"));
            org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("Sum check (2 + 3): 5"));
        }
    }

    private ServerConfig testConfig() {
        return new ServerConfig(
                "127.0.0.1",
                0,
                4,
                16,
                5000,
                300,
                16384,
                1048576,
                "public",
                "templates",
                Map.of(
                        "html", "text/html; charset=UTF-8",
                        "txt", "text/plain; charset=UTF-8",
                        "smscr", "text/plain; charset=UTF-8"
                )
        );
    }

    private Router testRouter() {
        Router router = new Router();
        DefaultRoutes.register(router, new TemplateService("templates"));
        return router;
    }
}
