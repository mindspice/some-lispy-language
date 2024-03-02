package parse.node;

import parse.token.TokenType;

import java.util.List;


public sealed interface Definition extends Node {

    record FunctionDef(String name, LambdaDef lambda) implements ExpressionNode {
        public FunctionDef(String name, List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                Node body, String returnType) {
            this(name, new LambdaDef(modifiers, parameters, body, returnType));
        }
    }

    record VariableDef(String name, List<TokenType.Modifier> modifiers, String type, Node value) implements Definition { }

    record LambdaDef(List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                     Node body, String returnType) implements Definition { }

    record ParamDef(String name, String type, boolean optional, Node defaultValue) implements Definition {
        public boolean isNamed() { return name != null; }

        public boolean isOptional() { return optional; }

        public boolean hasDefaultValue() { return defaultValue != null; }

    }

}
