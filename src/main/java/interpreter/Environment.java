package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;

import java.util.Map;
import java.util.stream.Collectors;


public interface Environment {
    LiteralNode createBinding(String name, Binding binding);

    boolean hasBinding(String name);

    LiteralNode reassignBinding(String name, LiteralNode value);

    Binding lookupBinding(String name);

    LiteralNode getBinding(String name);

    LiteralNode getBinding(String name, String expectedType);

    Environment getParent();

    default String stringifyMap(Map<String, Binding> map) {
        return map.entrySet().stream().map(e -> String.format("Symbol: %s, Value: %s", e.getKey(), e.getValue().value()))
                .collect(Collectors.joining("\n"));
    }
}
