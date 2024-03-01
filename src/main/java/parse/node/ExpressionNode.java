package parse.node;

import language.types.data.Pair;

import java.util.List;


public sealed interface ExpressionNode extends Node {

    record ConsExpr(Node car, Node cdr) implements ExpressionNode { }

    record VariableAccess(String name) implements ExpressionNode { }

    record FunctionCall(String name, List<FuncArg> arguements) implements ExpressionNode { }

    record FuncArg(Node value, String name) implements ExpressionNode {
        public boolean isNamed() { return name != null; }
    }

    record IfExpr(CondBranch condBranch, Node elseBranch) implements ExpressionNode { }

    record CondBranch(Node condNode, Node thenNode) implements ExpressionNode { }

    record CondExpr(List<Pair<CondBranch, CondBranch>> condBranches, Node elseBranch) implements ExpressionNode {
        public boolean hasElse() { return elseBranch != null; }
    }

    record ForIExpr(int start, int end, int progAmount, CollectionNode collection) implements ExpressionNode { }

}
