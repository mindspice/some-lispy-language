package parse.node;

import parse.token.TokenType;

import java.util.List;


public sealed interface DefinitionNode extends Node {

    record FunctionDef(String name, LiteralNode.LambdaLit lambda) implements DefinitionNode {
        public FunctionDef(String name, List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                Node body, String returnType) {
            this(name, new LiteralNode.LambdaLit(new LambdaDef(modifiers, parameters, body, returnType)));
        }
    }

    record VariableDef(String name, List<TokenType.Modifier> modifiers, String type, Node value) implements DefinitionNode { }

    record LambdaDef(List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                     Node body, String returnType) implements DefinitionNode {
        public int minArity() {
            if (parameters == null) { return 0; }
            int arity = 0;
            for (int i = 0; i < parameters.size(); ++i) {
                if (!parameters.get(i).optional) {
                    arity++;
                } else { break; }
            }
            return arity;
        }

        public int maxArity(){
            if (parameters == null) { return 0; }
            return parameters().size();
        }
    }

    record ParamDef(String name, String type, boolean optional, Node defaultValue, boolean dynamic, boolean mutable) {

        public boolean isOptional() { return optional; }

        public boolean hasDefaultValue() { return defaultValue != null; }

    }

}
