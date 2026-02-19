# 04 Routing

## Core Concepts

Routing is method + path pattern matching.

- Exact patterns: `/hello`
- Parameterized patterns: `/users/{id}`

The router returns `Optional<HttpResponse>`.

## Matching Rules

1. HTTP method must match.
2. Segment count must match.
3. Literal segments must match exactly.
4. `{name}` segments capture the value into path params.

Example:

- Pattern: `/users/{id}`
- Path: `/users/42`
- Extracted param: `id = 42`

## 405 Handling

If path matches but method does not, server returns `405 Method Not Allowed` and sets `Allow` header.

## Route Registration Example

```java
router.addRoute(HttpMethod.GET, "/users/{id}", context ->
    HttpResponse.json(HttpStatus.OK,
        "{\"userId\":\"" + JsonUtil.escape(context.pathParam("id").orElse("unknown")) + "\"}")
);
```

## Why No Annotations

Annotation-based routing is intentionally avoided to keep control flow obvious and avoid framework magic.
