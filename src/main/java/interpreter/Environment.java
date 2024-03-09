package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;

import java.util.Map;
import java.util.stream.Collectors;


public interface Environment {

    Environment getParent();

    LiteralNode createBinding(String name, Binding binding);

    boolean hasBinding(String name);

    Binding getBinding(String name);

    default String stringifyMap(Map<String, Binding> map) {
        return map.entrySet().stream().map(e -> String.format("Symbol: %s, Value: %s", e.getKey(), e.getValue().getClass()))
                .collect(Collectors.joining("\n"));
    }
}
