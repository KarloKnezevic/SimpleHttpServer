package com.example.simplehttpserver.script.ast;

/**
 * Expression token type used in echo and for-loop expressions.
 */
public enum ExpressionTokenType {
    VARIABLE,
    INTEGER,
    DOUBLE,
    STRING,
    FUNCTION,
    OPERATOR
}
