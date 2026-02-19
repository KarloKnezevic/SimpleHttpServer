package com.example.simplehttpserver.server;

import com.example.simplehttpserver.config.ServerConfig;
import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpParseException;
import com.example.simplehttpserver.http.HttpRequest;
import com.example.simplehttpserver.http.HttpRequestParser;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpResponseWriter;
import com.example.simplehttpserver.http.HttpStatus;
import com.example.simplehttpserver.routing.Router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles a single client socket: parse request, route it, build response, and write bytes.
 */
public final class ConnectionHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ConnectionHandler.class.getName());

    private final Socket socket;
    private final ServerConfig config;
    private final HttpRequestParser requestParser;
    private final HttpResponseWriter responseWriter;
    private final Router router;
    private final SessionManager sessionManager;
    private final StaticFileService staticFileService;
    private final ScriptService scriptService;

    public ConnectionHandler(
            Socket socket,
            ServerConfig config,
            HttpRequestParser requestParser,
            HttpResponseWriter responseWriter,
            Router router,
            SessionManager sessionManager,
            StaticFileService staticFileService,
            ScriptService scriptService
    ) {
        this.socket = socket;
        this.config = config;
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
        this.router = router;
        this.sessionManager = sessionManager;
        this.staticFileService = staticFileService;
        this.scriptService = scriptService;
    }

    @Override
    public void run() {
        HttpMethod requestMethod = HttpMethod.GET;
        String requestPath = "-";
        HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        try (socket;
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {

            socket.setSoTimeout(config.socketTimeoutMillis());

            try {
                HttpRequest request = requestParser.parse(
                        inputStream,
                        config.maxHeaderBytes(),
                        config.maxBodyBytes()
                );

                requestMethod = request.method();
                requestPath = request.path();

                SessionManager.SessionResolution sessionResolution = sessionManager.resolve(request);
                HttpResponse response = dispatchRequest(request, sessionResolution.session());

                if (sessionResolution.setCookieHeader().isPresent()) {
                    response = response.withHeader("Set-Cookie", sessionResolution.setCookieHeader().get());
                }

                responseStatus = response.status();
                responseWriter.write(outputStream, request.method(), response);
            } catch (HttpParseException parseException) {
                responseStatus = parseException.status();
                writeErrorResponse(outputStream, parseException.status(), parseException.getMessage());
            } catch (SocketTimeoutException timeoutException) {
                responseStatus = HttpStatus.REQUEST_TIMEOUT;
                writeErrorResponse(outputStream, HttpStatus.REQUEST_TIMEOUT,
                        "Connection timed out while reading request.");
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Unexpected request handling error", exception);
                responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                writeErrorResponse(outputStream, HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected server error.");
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.FINE, "Client connection closed before response was sent.", ioException);
        } finally {
            LOGGER.info(String.format("%s %s -> %d", requestMethod, requestPath, responseStatus.code()));
        }
    }

    private HttpResponse dispatchRequest(HttpRequest request, Session session) throws Exception {
        Optional<HttpResponse> routedResponse = router.dispatch(request, session);
        if (routedResponse.isPresent()) {
            return routedResponse.get();
        }

        Set<HttpMethod> allowedMethods = router.allowedMethods(request.path());
        if (!allowedMethods.isEmpty()) {
            String allowHeader = allowedMethods.stream().map(Enum::name).collect(Collectors.joining(", "));
            return HttpResponse.text(HttpStatus.METHOD_NOT_ALLOWED,
                    "Method " + request.method() + " is not allowed for " + request.path())
                    .withHeader("Allow", allowHeader);
        }

        Optional<HttpResponse> scriptResponse = scriptService.tryExecute(request.path(), request, session);
        if (scriptResponse.isPresent()) {
            return scriptResponse.get();
        }

        return staticFileService.tryServe(request.path())
                .orElseGet(() -> HttpResponse.text(HttpStatus.NOT_FOUND,
                        "No route or static file found for " + request.path()));
    }

    private void writeErrorResponse(OutputStream outputStream, HttpStatus status, String message) throws IOException {
        HttpResponse errorResponse = HttpResponse.text(status, message);
        responseWriter.write(outputStream, HttpMethod.GET, errorResponse);
    }
}
