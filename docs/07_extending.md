# 07 Extending the Server

## Add a New Route

1. Open `src/main/java/com/example/simplehttpserver/app/DefaultRoutes.java`.
2. Register a new route with `router.addRoute(...)`.
3. Build and test.

Example:

```java
router.addRoute(HttpMethod.GET, "/health", context ->
    HttpResponse.json(HttpStatus.OK, "{\"status\":\"UP\"}")
);
```

## Add a New Template

1. Create a file in `src/main/resources/templates`.
2. Render it from a route via `TemplateService.render(templateName, model)`.

## Add a New Script (`.smscr`)

1. Add a file under `src/main/resources/public/scripts`.
2. Use script syntax with `[$ ... $]` tags.
3. Call it directly via URL, for example:
   - `/scripts/my-demo.smscr`

No explicit route registration is required for `.smscr`; `ScriptService` handles those paths automatically.

## Add a New Static File

1. Put file in `src/main/resources/public`.
2. Access it directly by URL path.

## Add New MIME Type

Edit `src/main/resources/mime-types.properties`:

```properties
webp=image/webp
```

## Add a New Test

Place tests in matching package under `src/test/java`.

- Parsing behavior -> `http` tests
- Routing behavior -> `routing` tests
- End-to-end behavior -> `server` integration tests
