package interpreter;

import Evaluation.OperationVisitor;
import parse.Lexer;
import parse.Parser;
import parse.node.*;

import java.util.List;
import java.util.function.Function;


public class Interpreter {
    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();

    public String eval(String input) {
        var t = System.nanoTime();
        var tokens = lexer.process(input);
        //tokens.forEach(t -> System.out.println(t.type()));


        var ast = parser.process(tokens);
        evalProgram(ast);
        System.out.println("Eval Took: " + (System.nanoTime() - t));
        return "Eval Done";
//        return visitProgramNode(ast).asString();
    }

    void evalProgram(Node.Program program){
        program.topMost().forEach(n -> System.out.println(evalNode(n)));
    }

    Node evalNode(Node node) {
        return switch (node) {
            case DefinitionNode definitionNode -> null;
            case ExpressionNode expressionNode -> null;
            case LiteralNode literalNode -> node;
            case Node.Program program -> throw new RuntimeException("Fatal: Nested Program node, should never happen");
            case OperationNode operationNode -> evalOperationNode(operationNode);
        };
    }

    Node evalOperationNode(OperationNode operationNode) {
        EvalResult[] evalResults = new EvalResult[operationNode.operands().size()];
        List<Node> operands = operationNode.operands();
        for (int i = 0; i < operationNode.operands().size(); ++i) {
            if (evalNode(operands.get(i)) instanceof EvalResult result) {
                evalResults[i] = result;
            } else {
                throw new IllegalStateException("Invalided expression or literal provided for operation expression");
            }
        }
        Function<EvalResult[], LiteralNode> operation = OperationVisitor.operationMap.get(operationNode.getClass());
        return operation.apply(evalResults);
    }

//    Node evalExpressionNode(ExpressionNode expressionNode) {
//        switch (expressionNode) {
//            case ExpressionNode.AssignOp assignOp -> { }
//            case ExpressionNode.CondBranch condBranch -> { }
//            case ExpressionNode.CondExpr condExpr -> { }
//            case ExpressionNode.ConsExpr consExpr -> { }
//            case ExpressionNode.ForEachExpr forEachExpr -> { }
//            case ExpressionNode.ForIExpr forIExpr -> { }
//            case ExpressionNode.FuncArg funcArg -> { }
//            case ExpressionNode.FunctionCall functionCall -> { }
//            case ExpressionNode.IfExpr ifExpr -> { }
//            case ExpressionNode.MultiExpr multiExpr -> { }
//            case ExpressionNode.PrintExpr printExpr -> { }
//            case ExpressionNode.VariableAccess variableAccess -> { }
//            case ExpressionNode.While aWhile -> { }
//        }
//    }
}
