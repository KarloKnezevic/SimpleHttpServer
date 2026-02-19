# 02 HTTP Basics in This Project

## Request Model

The parser expects standard HTTP/1.x request format:

```text
<METHOD> <TARGET> <VERSION>\r\n
Header-Name: value\r\n
Header-Name: value\r\n
\r\n
[optional body based on Content-Length]
```

Supported methods:

- `GET`
- `POST`
- `HEAD`

Unsupported methods are treated as `501 Not Implemented`.

## Response Model

Each response includes:

- Status line (`HTTP/1.1 <code> <reason>`)
- Headers
- Blank line
- Optional body

Automatically added headers (if missing):

- `Date`
- `Server`
- `Connection: close`
- `Content-Length`

## Status Codes Commonly Used

- `200 OK`
- `400 Bad Request`
- `403 Forbidden`
- `404 Not Found`
- `405 Method Not Allowed`
- `408 Request Timeout`
- `413 Payload Too Large`
- `500 Internal Server Error`
- `501 Not Implemented`
- `503 Service Unavailable`

## Header Handling

Headers are parsed into a case-insensitive map of `name -> list of values`.

`Content-Length` rules:

- Must be numeric and non-negative
- Multiple different values are rejected (`400`)

`Transfer-Encoding: chunked` is explicitly rejected (`501`) to keep parser logic educational and compact.
