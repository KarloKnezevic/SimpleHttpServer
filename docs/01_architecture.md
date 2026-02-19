# 01 Architecture

## Goals

The architecture is intentionally explicit so students can follow the full HTTP lifecycle without framework abstraction.

## Main Components

- `SimpleHttpServer`: owns server socket, accept loop, and bounded worker pool.
- `ConnectionHandler`: handles exactly one client socket.
- `HttpRequestParser`: converts bytes into an `HttpRequest` object.
- `Router`: matches method + path patterns to handlers.
- `SessionManager`: creates/refreshes in-memory sessions from `SID` cookie.
- `ScriptService`: loads `.smscr` resources and runs script execution pipeline.
- `ScriptParser` + `ScriptEngine`: parse and execute the custom scripting language.
- `StaticFileService`: serves classpath static files and blocks path traversal.
- `HttpResponseWriter`: serializes status line, headers, and body bytes.

## Request Lifecycle

```mermaid
sequenceDiagram
    participant C as Client
    participant S as SimpleHttpServer
    participant W as Worker(ConnectionHandler)
    participant P as HttpRequestParser
    participant R as Router
    participant SS as ScriptService
    participant SF as StaticFileService
    participant RW as HttpResponseWriter

    C->>S: TCP connect + HTTP request bytes
    S->>W: Submit socket to worker pool
    W->>P: Parse request line, headers, body
    W->>R: Try route dispatch
    alt Route found
        R-->>W: HttpResponse
    else No route
        W->>SS: Try script (.smscr)
        alt Script request
            SS-->>W: HttpResponse/empty
        else Not a script or not found
            W->>SF: Try static file
            SF-->>W: HttpResponse/empty
        end
    end
    W->>RW: Serialize response
    RW-->>C: Status + headers + body
```

## Concurrency Model

- One accept thread.
- Bounded `ThreadPoolExecutor` for request handling.
- If queue is full, server responds with `503 Service Unavailable`.

Why this model:

- Easy to understand compared to event loops.
- Safe enough for small educational load.
- Demonstrates backpressure via bounded queue.

## Error Handling Strategy

- Parse failures return structured 4xx/5xx responses.
- Runtime exceptions in handlers return `500 Internal Server Error`.
- Socket timeouts return `408 Request Timeout`.
