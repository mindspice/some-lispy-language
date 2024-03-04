package interpreter;

import interpreter.data.Binding;

import java.util.HashMap;
import java.util.Map;


public class Environment {
    final Environment parentEnv;
    private Map<String, Binding> bindings = new HashMap<>(50);

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
            binding.reAssign(binding.type(), binding.value());
        }
        return binding.value();
    }

    private Binding lookupBinding(String name) {
        Binding existing = bindings.get(name);
        if (existing == null && parentEnv != null) {
            return parentEnv.lookupBinding(name);
        }
        return existing;
    }

    public Object getBinding(String name) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            throw new IllegalStateException("Undefined value");
        }
        return existing.value();
    }

    public Object getBinding(String name, String expectedType) {
        Binding existing = lookupBinding(name);
        if (existing == null) {
            throw new IllegalStateException("Undefined value");
        }
        if (!existing.type().equals(expectedType)) {
            throw new IllegalStateException(
                    String.format("Type mismatch expected: %s, found: %s", existing.type(), expectedType)
            );
        }
        return existing.value();
    }

}
