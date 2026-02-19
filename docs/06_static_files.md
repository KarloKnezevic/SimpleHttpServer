# 06 Static Files

## Static Root

Static resources are served from classpath root configured by:

- `server.publicResourcesRoot` (default `public`)

Files are under:

- `src/main/resources/public`

Note: `.smscr` files under this root are executed by `ScriptService` before static fallback, while other file types are served as static bytes.

## MIME Type Resolution

MIME mappings are loaded from:

- `src/main/resources/mime-types.properties`

Unknown extension fallback:

- `application/octet-stream`

## Default Index

Requests to `/` are mapped to `/index.html`.

## Path Traversal Protection

Before loading a resource:

1. Strip leading `/`
2. Normalize path (`Path.normalize()`)
3. Reject absolute paths or paths starting with `..`

Traversal attempts return `403 Forbidden`.

## Educational Scope

This service intentionally avoids advanced features such as range requests, ETags, and compression to keep internal flow simple.
