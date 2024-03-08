package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;

import java.util.HashMap;
import java.util.Map;


public class ClosureEnv implements Environment {
    Environment parentEnv;
    Environment closureEnv;
    private final Map<String, Binding> bindings = new HashMap<>(5);

    public ClosureEnv(Environment parentEnv, Environment closureEnv) {
        this.parentEnv = parentEnv;
        this.closureEnv = closureEnv;
    }

    public Environment getParent() {
        return parentEnv;
    }

    @Override
    public LiteralNode createBinding(String name, Binding binding) {
        Binding existing = bindings.get(name);
        if (existing == null) {
            bindings.put(name, binding);
        } else {
            throw new IllegalStateException("Redefinition of existing binding: " + name);
        }
        return binding.value();
    }

    @Override
    public boolean hasBinding(String name) {
        if (bindings.containsKey(name)) { return true; }
        if (closureEnv.hasBinding(name)) { return true; }
        if (parentEnv.hasBinding(name)) { return true; }
        return false;
    }

    @Override
    public LiteralNode reassignBinding(String name, LiteralNode value) {
        Binding existing = bindings.get(name);
        if (existing != null) {
            return existing.reAssign(value);
        }
        existing = closureEnv.lookupBinding(name);
        if (existing != null) {
            return existing.reAssign(value);
        }
        existing = parentEnv.lookupBinding(name);
        if (existing == null) {
          //  System.out.println(this);
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        existing.reAssign(value);
        return value;
    }

    @Override
    public Binding lookupBinding(String name) {
        Binding existing = bindings.get(name);
        if (existing != null) { return existing; }

        existing = closureEnv.lookupBinding(name);
        if (existing != null) { return existing; }

        existing = parentEnv.lookupBinding(name);
        if (existing == null) {
            //System.out.println(this);
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        return existing;
    }

    @Override
    public LiteralNode getBinding(String name) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            existing = closureEnv.lookupBinding(name);
        }
        if (existing == null) {
            existing = parentEnv.lookupBinding(name);
        }

        if (existing == null) {
            // System.out.println(this);
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        return existing.value();
    }

    @Override
    public LiteralNode getBinding(String name, String expectedType) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            existing = closureEnv.lookupBinding(name);
        }
        if (existing == null) {
            existing = parentEnv.lookupBinding(name);
        }
        if (existing == null) {
           // System.out.println(this);
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        if (!existing.type().equals(expectedType)) {
            throw new IllegalStateException(
                    String.format("Type mismatch expected: %s, found: %s", existing.type(), expectedType)
            );
        }
        return existing.value();
    }

    public String toString() {
        return "== Local Environment ==\n" + stringifyMap(bindings)
                + "\n== Closure Environment ==\n" + closureEnv.toString()
                + "\n== Parent Environment ==\n" + parentEnv.toString();
    }
}

