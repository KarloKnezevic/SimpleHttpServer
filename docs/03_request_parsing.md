# 03 Request Parsing

## Parser Steps

`HttpRequestParser` performs parsing in this order:

1. Read header bytes until empty line (`\r\n\r\n` or `\n\n`).
2. Enforce `maxHeaderBytes` limit.
3. Parse request line into method, target, version.
4. Parse headers into case-insensitive map.
5. Validate `Content-Length`.
6. Read body using exact `Content-Length` bytes.
7. Decode path and query parameters.

## Request Target Parsing

For `/echo?x=1&name=Alice`:

- Path: `/echo`
- Query map: `x -> ["1"]`, `name -> ["Alice"]`

Path decoding and query decoding both use UTF-8.

## Edge Cases Covered

- Missing/invalid request line -> `400`
- Invalid header syntax -> `400`
- Invalid URL encoding -> `400`
- Header too large -> `413`
- Body too large -> `413`
- Body shorter than `Content-Length` -> `400`
- Unsupported HTTP method -> `501`
- Unsupported `Transfer-Encoding: chunked` -> `501`

## Why This Implementation

It is intentionally strict and small so students can trace every validation rule and understand where each HTTP error code comes from.
