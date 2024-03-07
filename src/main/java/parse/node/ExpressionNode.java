package parse.node;

import interpreter.Environment;
import interpreter.Interpreter;
import interpreter.data.Binding;

import java.util.List;


public sealed interface ExpressionNode extends Node {

    record AssignOp(String name, Node value) implements ExpressionNode { }

    record MultiExpr(List<Node> expressions) implements ExpressionNode { }

    record PrintExpr(Node value) implements ExpressionNode { }

    record IfExpr(CondBranch condBranch, Node elseBranch) implements ExpressionNode {
        public boolean hasElse() { return elseBranch != null; }
    }

    record CondBranch(Node condNode, Node thenNode) { }

    record CondExpr(List<CondBranch> condBranches, Node elseBranch) implements ExpressionNode {
        public boolean hasElse() { return elseBranch != null; }
    }

    // record ForIExpr(int start, int end, int progAmount, CollectionNode collection, Node body) implements ExpressionNode { }

    // record ForEachExpr(CollectionNode collection, Node body) implements ExpressionNode { }

    record WhileLoopExpr(Node condition, Node body, boolean isDo) implements ExpressionNode { }

    record ConsExpr(Node car, Node cdr) implements ExpressionNode { }

    record FunctionCall(String name, List<FuncArg> arguments) implements ExpressionNode {
        // TODO FIXME this needs to be more streamlined and efficient
        public Environment getEnvironment(Interpreter interpreter, Environment currEnvironment) {
            var funcEnv = new Environment(currEnvironment);
            var lambda = (DefinitionNode.LambdaDef) currEnvironment.getBinding(name).asObject();
            if (arguments.size() < lambda.minArity() || arguments.size() > lambda.maxArity()) {
                throw new IllegalStateException(String.format("Argument count mismatch. Passed: %d, Min: %d, Max: %d",
                        arguments.size(), lambda.minArity(), lambda.maxArity())
                );
            }
            for (int i = 0; i < arguments.size(); ++i) {
                var arg = arguments.get(i);
                var param = lambda.parameters().get(i);
                var evaledArg = (LiteralNode) interpreter.evalNode(arg.value);
                funcEnv.createBinding(
                        arg.isNamed() ? arg.name() : param.name(),
                        new Binding(evaledArg.langType(), evaledArg, param.dynamic(), param.mutable())
                );
            }
            for (int i = 0; i < lambda.parameters().size(); ++i) {
                var param = lambda.parameters().get(i);
                if (param.isOptional()) { break; }
                if (!funcEnv.hasBinding(param.name())) {
                    throw new IllegalStateException("Required parameter needed for function call");
                }
            }
            return funcEnv;
        }
    }

    record LiteralCall(String name) implements ExpressionNode { }

    record FuncArg(Node value, String name) {
        public boolean isNamed() { return name != null; }
    }

}
