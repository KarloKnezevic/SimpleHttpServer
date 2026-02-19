package com.example.simplehttpserver.app;

import com.example.simplehttpserver.config.ServerConfig;
import com.example.simplehttpserver.config.ServerConfigLoader;
import com.example.simplehttpserver.routing.Router;
import com.example.simplehttpserver.server.SimpleHttpServer;
import com.example.simplehttpserver.template.TemplateService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * CLI entry point. Start the server and type "stop" in console to shut it down.
 */
public final class SimpleHttpServerApplication {

    private static final Logger LOGGER = Logger.getLogger(SimpleHttpServerApplication.class.getName());

    private SimpleHttpServerApplication() {
    }

    public static void main(String[] args) throws IOException {
        Path externalConfig = args.length > 0 ? Path.of(args[0]) : null;
        ServerConfig config = ServerConfigLoader.load(externalConfig);

        Router router = new Router();
        TemplateService templateService = new TemplateService(config.templatesRoot());
        DefaultRoutes.register(router, templateService);

        try (SimpleHttpServer server = new SimpleHttpServer(config, router);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            server.start();
            LOGGER.info(() -> "Server started on http://" + config.host() + ":" + server.getBoundPort());
            LOGGER.info("Type 'stop' and press Enter to stop the server.");

            while (true) {
                String line = console.readLine();
                if (line == null) {
                    continue;
                }
                if ("stop".equalsIgnoreCase(line.trim())) {
                    break;
                }
            }
        }
    }
}
