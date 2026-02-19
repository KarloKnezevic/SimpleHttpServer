package com.example.simplehttpserver.script.runtime;

/**
 * Numeric wrapper used for script arithmetic operations.
 */
public class ValueWrapper {

    private Object value;

    public ValueWrapper(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void increment(Object incrementValue) {
        value = apply('+', value, incrementValue);
    }

    public void decrement(Object decrementValue) {
        value = apply('-', value, decrementValue);
    }

    public void multiply(Object multiplicationValue) {
        value = apply('*', value, multiplicationValue);
    }

    public void divide(Object divisionValue) {
        value = apply('/', value, divisionValue);
    }

    public int numCompare(Object withValue) {
        Number left = toNumber(value);
        Number right = toNumber(withValue);
        return Double.compare(left.doubleValue(), right.doubleValue());
    }

    private Object apply(char operator, Object leftInput, Object rightInput) {
        Number left = toNumber(leftInput);
        Number right = toNumber(rightInput);

        boolean floatingResult = isFloating(leftInput) || isFloating(rightInput) || operator == '/';

        return switch (operator) {
            case '+' -> {
                if (floatingResult) {
                    yield left.doubleValue() + right.doubleValue();
                }
                yield left.intValue() + right.intValue();
            }
            case '-' -> {
                if (floatingResult) {
                    yield left.doubleValue() - right.doubleValue();
                }
                yield left.intValue() - right.intValue();
            }
            case '*' -> {
                if (floatingResult) {
                    yield left.doubleValue() * right.doubleValue();
                }
                yield left.intValue() * right.intValue();
            }
            case '/' -> {
                if (right.doubleValue() == 0.0) {
                    throw new ScriptRuntimeException("Division by zero.");
                }
                yield left.doubleValue() / right.doubleValue();
            }
            default -> throw new ScriptRuntimeException("Unsupported operator: " + operator);
        };
    }

    private Number toNumber(Object input) {
        if (input == null) {
            return 0;
        }
        if (input instanceof Integer integer) {
            return integer;
        }
        if (input instanceof Double doubles) {
            return doubles;
        }
        if (input instanceof Number number) {
            return number.doubleValue();
        }
        if (input instanceof String stringValue) {
            return parseNumericString(stringValue);
        }

        throw new ScriptRuntimeException("Value is not numeric: " + input);
    }

    private Number parseNumericString(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }

        try {
            if (trimmed.contains(".") || trimmed.contains("e") || trimmed.contains("E")) {
                return Double.parseDouble(trimmed);
            }
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException exception) {
            throw new ScriptRuntimeException("String value is not numeric: " + value, exception);
        }
    }

    private boolean isFloating(Object input) {
        if (input instanceof Double || input instanceof Float) {
            return true;
        }
        if (input instanceof String stringValue) {
            String trimmed = stringValue.trim();
            return trimmed.contains(".") || trimmed.contains("e") || trimmed.contains("E");
        }
        return false;
    }
}
