# 05 Scripting / Templating

## Overview

This project now supports both:

- a **custom educational scripting language** (`.smscr`)
- a minimal **HTML placeholder templating** mechanism (`{{key}}`)

The scripting language is the primary demonstration of server-side execution flow.

## Script Language Architecture

The implementation is intentionally explicit and split into three core stages:

1. **Lexer**
   - `ScriptDocumentLexer`: splits source into text and tag blocks.
   - `TagLexer`: tokenizes tag content.
2. **Parser**
   - `ScriptParser`: validates syntax and builds AST.
3. **Engine**
   - `ScriptEngine`: executes AST against runtime context.

## Script Syntax

### Tag delimiters

- Open: `[$`
- Close: `$]`

### Tags

- Echo tag: `[$= ... $]`
- For loop: `[$ FOR i 1 10 1 $] ... [$END$]`

### Echo expressions (RPN)

Echo uses stack-based Reverse Polish Notation.

Example:

```text
[$= 2 3 + $]
```

Outputs `5`.

## Supported Functions

- `@sin`
- `@decfmt`
- `@dup`
- `@setMimeType`
- `@paramGet`
- `@pparamGet`, `@pparamSet`, `@pparamDel`
- `@tparamGet`, `@tparamSet`, `@tparamDel`

## Runtime Context

Script runtime has access to:

- Query parameters from URL (`@paramGet`)
- Persistent session parameters (`@pparam*`)
- Temporary per-request parameters (`@tparam*`)
- Output stream
- Response MIME type

## Script Resource Mapping

If request path ends with `.smscr`, server uses `ScriptService` to:

1. load script from `src/main/resources/public/...`
2. parse and execute script
3. return generated response

## Example Script Files

- `src/main/resources/public/scripts/basic.smscr`
- `src/main/resources/public/scripts/fibonacci.smscr`
- `src/main/resources/public/scripts/callcount.smscr`

## Templating (Secondary Mechanism)

Templates are still available through `TemplateService` using `{{key}}` replacement.

This is intentionally simpler than scripting and mainly used for route-level HTML demos (`/template`).
