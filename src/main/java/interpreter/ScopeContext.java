package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;


public class ScopeContext {
    private Environment currEnv = new ScopeEnv();

    public Environment pushScope() {
        currEnv = new ScopeEnv(currEnv);
        return currEnv;
    }

    public Environment pushClosureScope(Environment closure) {
        currEnv = new ClosureEnv(currEnv, closure);
        return currEnv;
    }

    public Environment popScope() {
        if (currEnv.getParent() != null) {
            currEnv = currEnv.getParent();
        } else {
            throw new IllegalStateException("Attempting to pop the global environment");
        }
        return currEnv;
    }

    public LiteralNode lookupBinding(String name) {
        Binding found = currEnv.getBinding(name);
        if (found != null) {
            return found.value();
        } else {
            throw new IllegalStateException("Unbound symbol: " + name);
        }
    }

    public boolean hasBinding(String name) {
        return currEnv.hasBinding(name);
    }

    public LiteralNode createBinding(String name, Binding binding) {
        return currEnv.createBinding(name, binding);
    }

    public LiteralNode reassignBinding(String name, LiteralNode value) {
        Binding found = currEnv.getBinding(name);
        if (found == null) {
            throw new IllegalStateException("Attempted to re-assign non existing symbol: " + name);
        }
        return found.reAssign(value);
    }

    public Environment getCurrEnv() {
        return currEnv;
    }

    public String toString() {
        return currEnv.toString();
    }

}
