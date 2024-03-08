package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;

import java.util.HashMap;
import java.util.Map;


public class ScopeEnv implements Environment {
    final Environment parentEnv;
    private final Map<String, Binding> bindings = new HashMap<>();

    public ScopeEnv() {
        parentEnv = null;
    }

    public ScopeEnv(Environment parentEnv) {
        this.parentEnv = parentEnv;
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
          //  System.out.println(this);
            throw new IllegalStateException("Redefinition of existing binding: " + name);
        }
        return binding.value();
    }

    @Override
    public boolean hasBinding(String name) {
        if (parentEnv == null) {
            return bindings.containsKey(name);
        }
        if (bindings.containsKey(name)) {
            return true;
        } else {
            return parentEnv.hasBinding(name);
        }
    }

    @Override
    public LiteralNode reassignBinding(String name, LiteralNode value) {
        Binding existing = bindings.get(name);
        if (existing == null && parentEnv != null) {
            existing = parentEnv.lookupBinding(name);
        }
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
        if (existing == null && parentEnv != null) {
            return parentEnv.lookupBinding(name);
        }

        if (existing == null) {
          //  System.out.println(this);
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        return existing;
    }

    @Override
    public LiteralNode getBinding(String name) {
        Binding existing = lookupBinding(name);
        if (existing == null && parentEnv != null) {
            existing = parentEnv.lookupBinding(name);
        }
        if (existing == null) {
            throw new IllegalStateException("Undefined symbol: " + name);
        }
        return existing.value();
    }

    @Override
    public LiteralNode getBinding(String name, String expectedType) {
        Binding existing = lookupBinding(name);
        if (existing == null && parentEnv != null) {
            existing = parentEnv.lookupBinding(name);
        }
        if (existing == null) {
         //   System.out.println(this);
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
        String s = "== Scope Environment==\n" + stringifyMap(bindings);
        s += parentEnv == null ? "\n Null Parent" : "\n== Parent Environment ==\n"+ parentEnv.toString();
        return s;
    }

}
