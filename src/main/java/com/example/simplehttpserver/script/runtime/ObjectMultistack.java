package com.example.simplehttpserver.script.runtime;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stack of values per variable name.
 */
public class ObjectMultistack {

    private final Map<String, Deque<ValueWrapper>> stacks = new ConcurrentHashMap<>();

    public void push(String name, ValueWrapper valueWrapper) {
        stacks.computeIfAbsent(name, ignored -> new ArrayDeque<>()).push(valueWrapper);
    }

    public ValueWrapper pop(String name) {
        Deque<ValueWrapper> stack = stacks.get(name);
        if (stack == null || stack.isEmpty()) {
            throw new ScriptRuntimeException("No value on stack for variable: " + name);
        }

        ValueWrapper value = stack.pop();
        if (stack.isEmpty()) {
            stacks.remove(name);
        }
        return value;
    }

    public ValueWrapper peek(String name) {
        Deque<ValueWrapper> stack = stacks.get(name);
        if (stack == null || stack.isEmpty()) {
            throw new ScriptRuntimeException("No value on stack for variable: " + name);
        }
        return stack.peek();
    }
}
