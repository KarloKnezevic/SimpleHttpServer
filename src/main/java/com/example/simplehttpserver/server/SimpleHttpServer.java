package com.example.simplehttpserver.server;

import com.example.simplehttpserver.config.ServerConfig;
import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpRequestParser;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpResponseWriter;
import com.example.simplehttpserver.http.HttpStatus;
import com.example.simplehttpserver.routing.Router;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core server class: owns the accept loop and delegates each client socket to a worker pool.
 */
public final class SimpleHttpServer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(SimpleHttpServer.class.getName());

    private final ServerConfig config;
    private final Router router;
    private final HttpRequestParser requestParser = new HttpRequestParser();
    private final HttpResponseWriter responseWriter = new HttpResponseWriter();
    private final SessionManager sessionManager;
    private final StaticFileService staticFileService;
    private final ScriptService scriptService;
    private final ThreadPoolExecutor workerPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private Thread acceptThread;

    public SimpleHttpServer(ServerConfig config, Router router) {
        this.config = config;
        this.router = router;
        this.sessionManager = new SessionManager(config.sessionTimeoutSeconds());
        this.staticFileService = new StaticFileService(config.publicResourcesRoot(), config.mimeTypes());
        this.scriptService = new ScriptService(config.publicResourcesRoot());
        this.workerPool = new ThreadPoolExecutor(
                config.workerThreads(),
                config.workerThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(config.queueCapacity())
        );
    }

    public synchronized void start() throws IOException {
        if (running.get()) {
            return;
        }

        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(config.host(), config.port()));
        running.set(true);

        acceptThread = new Thread(this::acceptLoop, "simple-http-server-accept");
        acceptThread.start();

        LOGGER.info(() -> "Server listening on " + config.host() + ":" + getBoundPort());
    }

    public synchronized void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException exception) {
                LOGGER.log(Level.WARNING, "Failed to close server socket", exception);
            }
        }

        if (acceptThread != null) {
            try {
                acceptThread.join(2000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            workerPool.shutdownNow();
        }

        sessionManager.close();
        LOGGER.info("Server stopped.");
    }

    public int getBoundPort() {
        if (serverSocket == null) {
            return config.port();
        }
        return serverSocket.getLocalPort();
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                try {
                    workerPool.execute(new ConnectionHandler(
                            socket,
                            config,
                            requestParser,
                            responseWriter,
                            router,
                            sessionManager,
                            staticFileService,
                            scriptService
                    ));
                } catch (RejectedExecutionException rejectedExecutionException) {
                    writeBusyResponseAndClose(socket);
                }
            } catch (SocketException socketException) {
                if (running.get()) {
                    LOGGER.log(Level.WARNING, "Socket exception in accept loop", socketException);
                }
            } catch (IOException ioException) {
                if (running.get()) {
                    LOGGER.log(Level.WARNING, "I/O exception in accept loop", ioException);
                }
            }
        }
    }

    private void writeBusyResponseAndClose(Socket socket) {
        try (socket; OutputStream outputStream = socket.getOutputStream()) {
            HttpResponse busyResponse = HttpResponse.text(HttpStatus.SERVICE_UNAVAILABLE,
                    "Server is busy. Try again shortly.");
            responseWriter.write(outputStream, HttpMethod.GET, busyResponse);
        } catch (IOException exception) {
            LOGGER.log(Level.FINE, "Unable to send busy response", exception);
        }
    }

    @Override
    public void close() {
        stop();
    }
}
