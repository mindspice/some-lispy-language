package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;

import java.util.HashMap;
import java.util.Map;


public class Environment {
    final Environment parentEnv;
    private final Map<String, Binding> bindings = new HashMap<>();

    public Environment() {
        parentEnv = null;
    }

    public Environment(Environment parentEnv) {
        this.parentEnv = parentEnv;
    }

    public Object createBinding(String name, Binding binding) {
        Binding existing = bindings.get(name);
        if (existing == null) {
            bindings.put(name, binding);
        } else {
           throw new IllegalStateException("Redefinition of existing binding");
        }
        return binding.value();
    }

    public boolean hasBinding(String name) {
        return bindings.containsKey(name);
    }

    public LiteralNode reassignBinding(String name, LiteralNode value){
        Binding existing = bindings.get(name);
        if (existing == null) {
            throw new IllegalStateException("Attempted to reassign to non-existing our out of scope binding");
        }
        existing.reAssign(value);
        return value;
    }

    private Binding lookupBinding(String name) {
        Binding existing = bindings.get(name);
        if (existing == null && parentEnv != null) {
            return parentEnv.lookupBinding(name);
        }
        return existing;
    }

    public LiteralNode getBinding(String name) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            throw new IllegalStateException("Undefined symbol");
        }
        return existing.value();
    }

    public LiteralNode getBinding(String name, String expectedType) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            throw new IllegalStateException("Undefined symbol");
        }
        if (!existing.type().equals(expectedType)) {
            throw new IllegalStateException(
                    String.format("Type mismatch expected: %s, found: %s", existing.type(), expectedType)
            );
        }
        return existing.value();
    }

}
