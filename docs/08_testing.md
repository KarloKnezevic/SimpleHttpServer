# 08 Testing

## Test Types

### Unit tests

- `HttpRequestParserTest`
  - request line parsing
  - header parsing
  - query parsing
  - invalid request handling
- `RouterTest`
  - route matching
  - path parameter extraction
  - allowed method discovery
- `HttpResponseWriterTest`
  - status/header/body serialization
  - HEAD response body omission
- `ScriptDocumentLexerTest`
  - splitting text/tag blocks
- `ScriptParserTest`
  - FOR/END and ECHO AST parsing
- `ScriptEngineTest`
  - FOR execution
  - RPN expression evaluation
  - function handling (`@setMimeType`, `@paramGet`, etc.)

### Integration test

- `SimpleHttpServerIntegrationTest`
  - starts server on random port
  - performs real HTTP request to `/hello`
  - performs real HTTP request to `/scripts/basic.smscr`
  - verifies status code and response body

## Run Commands

Run all tests:

```bash
mvn test
```

Run a single test:

```bash
mvn -Dtest=HttpRequestParserTest test
```

## Notes

- Integration test uses Java `HttpClient` and local loopback only.
- No external network calls are required.
