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
        if (parentEnv != null && bindings.containsKey(name)) { // check for null parent to allow redefinition at global scope
            throw new IllegalStateException("Attempted to redefine existing symbol binding");
        }
        bindings.put(name, binding);
        return binding.value();
    }

    @Override
    public boolean hasBinding(String name) {
        if (bindings.containsKey(name)) {
            return true;
        }
        return parentEnv != null && parentEnv.hasBinding(name);
    }

    @Override
    public Binding getBinding(String name) {
        Binding found = bindings.get(name);
        if (found != null) { return found;}
        return parentEnv != null ? parentEnv.getBinding(name) : null;
    }

    public String toString() {
        String s = "== Scope Environment==\n" + stringifyMap(bindings);
        s += parentEnv == null ? "\n Null Parent" : "\n== Parent Environment ==\n" + parentEnv.toString();
        return s;
    }

}
