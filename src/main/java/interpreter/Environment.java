package interpreter;

import interpreter.data.Binding;
import parse.node.LiteralNode;


public interface Environment {
    LiteralNode createBinding(String name, Binding binding);

    boolean hasBinding(String name);

    LiteralNode reassignBinding(String name, LiteralNode value);

    Binding lookupBinding(String name);

    LiteralNode getBinding(String name);

    LiteralNode getBinding(String name, String expectedType);

    Environment getParent();

}
