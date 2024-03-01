package parse.node;

import parse.Modifier;

import java.util.List;


public sealed interface DefinitionNode extends Node {
    record FunctionDef(
            String name,
            List<Modifier> modifiers,
            List<ParameterDef> parameterDefs,
            List<Closure> closures,
            Node body,
            String returnType
    ) implements DefinitionNode { }

    record VariableDef(
            String name,
            String type,
            List<Modifier> modifiers,
            Node value
    ) implements DefinitionNode { }

    record LambdaDef(
            String internalName,
            List<Modifier> modifiers,
            List<ParameterDef> parameterDefs,
            List<Closure> closures,
            Node body,
            String returnType
    ) implements DefinitionNode { }

    record ParameterDef(
            String name,
            String type,
            Node defaultValue
    ) {

        public boolean isNamed() { return name != null; }

        public boolean hasDefaultValue() { return defaultValue != null; }

        public static ParameterDef ofUntyped(String name) {
            return new ParameterDef(name, "NONE", null);
        }

        public static ParameterDef ofTyped(String name, String type) {
            return new ParameterDef(name, type, null);
        }

        public static ParameterDef ofDefUnTypes(String name, Node value) {
            return new ParameterDef(name, "NONE", value);
        }

        public static ParameterDef ofDefTyped(String name, String type, Node value) {
            return new ParameterDef(name, type, value);
        }

    }

}
