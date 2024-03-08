package interpreter;

public class ScopeContext {
    private Environment currEnv = new ScopeEnv();
    private Environment prevScope;

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


    public Environment getScope() {
        return currEnv;
    }


    public String toString() {
        return currEnv.toString();
    }

}
