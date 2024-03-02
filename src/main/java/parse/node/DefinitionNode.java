package parse.node;

import parse.token.TokenType;

import java.util.List;


public sealed interface DefinitionNode extends Node {

    record FunctionDef(String name, LambdaDef lambda) implements DefinitionNode {
        public FunctionDef(String name, List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                Node body, String returnType) {
            this(name, new LambdaDef(modifiers, parameters, body, returnType));
        }
    }

    record VariableDef(String name, List<TokenType.Modifier> modifiers, String type, Node value) implements DefinitionNode { }

    record LambdaDef(List<TokenType.Modifier> modifiers, List<ParamDef> parameters,
                     Node body, String returnType) implements DefinitionNode { }

    record ParamDef(String name, String type, boolean optional, Node defaultValue) implements DefinitionNode {
        public boolean isNamed() { return name != null; }

        public boolean isOptional() { return optional; }

        public boolean hasDefaultValue() { return defaultValue != null; }

    }

}
