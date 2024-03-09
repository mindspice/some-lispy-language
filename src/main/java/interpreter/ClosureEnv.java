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
        if (bindings.containsKey(name)) {
            throw new IllegalStateException("Attempted to redefine existing symbol binding");
        }
        bindings.put(name, binding);
        return binding.value();
    }

    @Override
    public boolean hasBinding(String name) {
        if (bindings.containsKey(name)) { return true; }
        if (closureEnv.hasBinding(name)) { return true; }
        return parentEnv != null && parentEnv.hasBinding(name);
    }

    @Override
    public Binding getBinding(String name) {
        Binding found = bindings.get(name);
        if (found != null) {
            return found;
        } else {
            found = closureEnv.getBinding(name);
            if (found != null) { return found; }
        }
        return parentEnv != null ? parentEnv.getBinding(name) : null;
    }
}

