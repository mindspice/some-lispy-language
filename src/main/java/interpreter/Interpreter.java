package interpreter;

import evaluation.OperationEval;
import interpreter.data.Binding;
import parse.Lexer;
import parse.Parser;
import parse.node.*;
import parse.token.TokenType;

import java.util.List;
import java.util.function.Function;


public class Interpreter {
    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();
    private final Environment globalEnvironment = new Environment(null);

    public String eval(String input) {
        var t = System.nanoTime();
        var tokens = lexer.process(input);

        var ast = parser.process(tokens);
        System.out.println(ast);
        evalProgram(ast);
        return "Eval Took: " + (System.nanoTime() - t);
    }

    void evalProgram(Node.Program program) {
        for (int i = 0; i < program.topMost().size(); ++i) {
            Node evaledNode = evalNode(program.topMost().get(i));
            String evalString = evaledNode.toString();
            if (!evalString.isEmpty()) {
                System.out.println(evalString);
            }
        }
    }

    public Node evalNode(Node node) {
        return switch (node) {
            case DefinitionNode definitionNode -> evalDefinition(definitionNode);
            case ExpressionNode expressionNode -> evalExpressionNode(expressionNode);
            case OperationNode operationNode -> evalOperationNode(operationNode);
            case LiteralNode literalNode -> literalNode;
            case Node.Program program -> throw new RuntimeException("Fatal: Nested Program node, should never happen");

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
        Function<EvalResult[], LiteralNode> operation = OperationEval.operationMap.get(operationNode.getClass());
        return operation.apply(evalResults);
    }

    Node evalExpressionNode(ExpressionNode expressionNode) {
        return switch (expressionNode) {
            case ExpressionNode.AssignOp assignOp -> evalAssignment(assignOp);
            case ExpressionNode.CondExpr condExpr -> evalCondExpr(condExpr);
            case ExpressionNode.ConsExpr consExpr -> null;
            case ExpressionNode.FunctionCall functionCall -> null;
            case ExpressionNode.IfExpr ifExpr -> evalIfExpr(ifExpr);
            case ExpressionNode.MultiExpr multiExpr -> evalMultiExpression(multiExpr);
            case ExpressionNode.PrintExpr printExpr -> evalPrintExpression(printExpr);
            case ExpressionNode.WhileLoopExpr whileLoopExpr -> evalWhileExpression(whileLoopExpr);
            case ExpressionNode.LiteralCall literalCall -> evalLiteralCall(literalCall);
        };
    }

    Node evalFunctionCall(ExpressionNode.FunctionCall functionCall) {
        return null;
    }

    Node evalLiteralCall(ExpressionNode.LiteralCall literalCall) {
        return globalEnvironment.getBinding(literalCall.name());
    }

    Node evalPrintExpression(ExpressionNode.PrintExpr printExpr) {
        System.out.println(evalNode(printExpr.value()).toString());
        return LiteralNode.VOID; // TODO work on returns
    }

    private boolean containsModifier(List<TokenType.Modifier> modList, TokenType.Modifier... checkMods) {
        if (modList == null) { return false; }
        for (int i = 0; i < checkMods.length; ++i) {
            if (modList.contains(checkMods[i])) { return true; }
        }
        return false;
    }

    Node evalAssignment(ExpressionNode.AssignOp assignment) {
        LiteralNode evaledNode =  (LiteralNode) evalNode(assignment.value());
        if (evaledNode instanceof LiteralNode literalNode) {
            globalEnvironment.reassignBinding(assignment.name(), literalNode);
            return evaledNode;
        }
        throw new IllegalStateException("Invalid assignment, Expected lambda or literal found: " + evaledNode);
    }

    Node evalDefinition(DefinitionNode definitionNode) {
        return switch (definitionNode) {
            case DefinitionNode.VariableDef varDef -> {
                Node evaledNode = evalNode(varDef.value());
                // TODO: check that expression that evals to a lambda properly assigns
                if (evaledNode instanceof LiteralNode result) {
                    if (containsModifier(varDef.modifiers(), TokenType.Modifier.DYNAMIC, TokenType.Modifier.DYNAMIC_ALL)) {
                        globalEnvironment.createBinding(varDef.name(), Binding.ofDynamic(result));
                    } else if (containsModifier(varDef.modifiers(), TokenType.Modifier.MUTABLE, TokenType.Modifier.MUTABLE_ALL)) {
                        globalEnvironment.createBinding(varDef.name(), Binding.ofMutable(result));
                    } else {
                        globalEnvironment.createBinding(varDef.name(), Binding.ofFinal(result));
                    }
                    yield evaledNode;
                } else {
                    throw new IllegalStateException("Variable definition not instance of lambda or evaluate to a literal value");
                }
            }
            case DefinitionNode.FunctionDef func -> {
                if (containsModifier(func.lambda().value().modifiers(), TokenType.Modifier.DYNAMIC, TokenType.Modifier.DYNAMIC_ALL)) {
                    globalEnvironment.createBinding(func.name(), Binding.ofDynamic(func.lambda()));
                } else if (containsModifier(func.lambda().value().modifiers(), TokenType.Modifier.MUTABLE, TokenType.Modifier.MUTABLE_ALL)) {
                    globalEnvironment.createBinding(func.name(), Binding.ofMutable(func.lambda()));
                } else {
                    globalEnvironment.createBinding(func.name(), Binding.ofFinal(func.lambda()));
                }
                yield func.lambda();
            }
            case DefinitionNode.LambdaDef lambdaDef -> lambdaDef;
        };
    }

    Node evalWhileExpression(ExpressionNode.WhileLoopExpr whileLoop) {
        Node evaledNode = evalNode(whileLoop.condition());

        if (evaledNode instanceof EvalResult e) {
            if (!whileLoop.isDo() && !e.asBoolean()) { return LiteralNode.FALSE; }
        } else {
            throw new IllegalStateException("Loop condition invalid, not a boolean expression");
        }


        do {
            evaledNode = evalNode(whileLoop.body());
        } while (((EvalResult) evalNode(whileLoop.condition())).asBoolean());
        return evaledNode;
    }

    Node evalMultiExpression(ExpressionNode.MultiExpr multiExpr) {
        var exprList = multiExpr.expressions();
        Node evaledNode = null;
        for (int i = 0; i < exprList.size(); ++i) {
            evaledNode = evalNode(exprList.get(i));
        }
        return evaledNode;
    }

    Node evalIfExpr(ExpressionNode.IfExpr ifExpr) {
        if (evalNode(ifExpr.condBranch().condNode()) instanceof LiteralNode.BooleanLit result) {
            if (result.asBoolean()) {
                return evalNode(ifExpr.condBranch().thenNode());
            } else {
                return ifExpr.hasElse() ? evalNode(ifExpr.elseBranch()) : LiteralNode.FALSE;
            }
        } else {
            throw new IllegalStateException("Invalided expression for if statement");
        }
    }

    Node evalCondExpr(ExpressionNode.CondExpr condExpr) {
        for (int i = 0; i < condExpr.condBranches().size(); ++i) {
            var branch = condExpr.condBranches().get(i);
            if (evalNode(branch.condNode()) instanceof LiteralNode.BooleanLit result) {
                if (result.asBoolean()) { return evalNode(branch.thenNode()); }
            }
        }
        return condExpr.hasElse() ? evalNode(condExpr.elseBranch()) : LiteralNode.FALSE;
    }
}
