package interpreter;

import evaluation.OperationEval;
import interpreter.data.Binding;
import language.types.data.Pair;
import parse.Lexer;
import parse.Parser;
import parse.node.*;
import parse.token.TokenType;

import java.util.List;
import java.util.function.Function;


public class Interpreter {
    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();
    private final ScopeContext env = new ScopeContext();

    public String eval(String input) {
        var t = System.nanoTime();
        var tokens = lexer.process(input);
        tokens.forEach(tk -> System.out.print(tk.type() + ","));
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
            case ExpressionNode.ConsExpr consExpr -> evalCons(consExpr);
            case ExpressionNode.FunctionCall functionCall -> evalFunctionCall(functionCall);
            case ExpressionNode.ListAccess listAccess -> evalListAccess(listAccess);
            case ExpressionNode.IfExpr ifExpr -> evalIfExpr(ifExpr);
            case ExpressionNode.PairListExpression pairListExpr -> evalPairList(pairListExpr);
            case ExpressionNode.MultiExpr multiExpr -> evalMultiExpression(multiExpr);
            case ExpressionNode.PrintExpr printExpr -> evalPrintExpression(printExpr);
            case ExpressionNode.WhileLoopExpr whileLoopExpr -> evalWhileExpression(whileLoopExpr);
            case ExpressionNode.LiteralCall literalCall -> evalLiteralCall(literalCall);
        };
    }

    Node evalPairList(ExpressionNode.PairListExpression listExpr) {
        var list = listExpr.elements();
        Pair<?, ?> head = Pair.of(list.getLast(), LiteralNode.NIL_LIST);
        for (int i = list.size() - 2; i >= 0; --i) {
            head = Pair.of(list.get(i), head);
        }
        return new LiteralNode.PairLit(head);
    }

    Node evalCons(ExpressionNode.ConsExpr consExpr) {
        Node evaledCar = evalNode(consExpr.car());
        Node evaledCdr = evalNode(consExpr.cdr());
        return LiteralNode.PairLit.of(evaledCar, evaledCdr);
    }

    Node evalListAccess(ExpressionNode.ListAccess listAccess) {
        if (!(evalNode(listAccess.list()) instanceof LiteralNode.PairLit pair)) {
            throw new IllegalStateException("Attempted list access of non-list object");
        }

        String pattern = listAccess.indexExpr() == null
                         ? listAccess.pattern()
                         : "f" + "r".repeat(((LiteralNode) evalNode(listAccess.indexExpr())).asInt());

        Object value = pattern.charAt(pattern.length() - 1) == 'f' ? pair.value().car() : pair.value().cdr();
        for (int i = pattern.length() - 2; i >= 0; --i) {
            if (value instanceof Pair<?, ?> currPair) {
                value = pattern.charAt(i) == 'f' ? currPair.car() : currPair.cdr();
            } else {
                throw new IllegalStateException("Invalid access pattern");
            }
        }
        if (value instanceof Pair<?, ?> p) { return new LiteralNode.PairLit(p); }
        return (LiteralNode) value;
    }

    Node evalFunctionCall(ExpressionNode.FunctionCall functionCall) {
        LiteralNode literal = env.getScope().getBinding(functionCall.name());
        if (literal instanceof LiteralNode.LambdaLit lambda) {
            try {
                env.pushClosureScope(lambda.env());
                functionCall.bindParameters(this, lambda.value(), env.getScope());
                return evalNode(lambda.value().body());
            } finally {
                env.popScope();
            }
        }
        throw new IllegalStateException(
                String.format("Attempted to call non lambda bound symbol %s as function", functionCall.name())
        );
    }

    Node evalLiteralCall(ExpressionNode.LiteralCall literalCall) {
        return env.getScope().getBinding(literalCall.name());
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
        LiteralNode evaledNode = (LiteralNode) evalNode(assignment.value());
        if (evaledNode instanceof LiteralNode literalNode) {
            env.getScope().reassignBinding(assignment.name(), literalNode);
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
                        env.getScope().createBinding(varDef.name(), Binding.ofDynamic(result));
                    } else if (containsModifier(varDef.modifiers(), TokenType.Modifier.MUTABLE, TokenType.Modifier.MUTABLE_ALL)) {
                        env.getScope().createBinding(varDef.name(), Binding.ofMutable(result));
                    } else {
                        env.getScope().createBinding(varDef.name(), Binding.ofFinal(result));
                    }
                    yield evaledNode;
                } else {
                    throw new IllegalStateException("Variable definition not instance of lambda or evaluate to a literal value");
                }
            }
            case DefinitionNode.FunctionDef func -> {
                LiteralNode.LambdaLit lambdaLit = new LiteralNode.LambdaLit(func.lambda(), env.getScope());
                if (containsModifier(func.lambda().modifiers(), TokenType.Modifier.DYNAMIC, TokenType.Modifier.DYNAMIC_ALL)) {
                    env.getScope().createBinding(func.name(), Binding.ofDynamic(lambdaLit));
                } else if (containsModifier(func.lambda().modifiers(), TokenType.Modifier.MUTABLE, TokenType.Modifier.MUTABLE_ALL)) {
                    env.getScope().createBinding(func.name(), Binding.ofMutable(lambdaLit));
                } else {
                    env.getScope().createBinding(func.name(), Binding.ofFinal(lambdaLit));
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
        try {
            env.pushScope();
            var exprList = multiExpr.expressions();
            Node evaledNode = null;
            for (int i = 0; i < exprList.size(); ++i) {
                evaledNode = evalNode(exprList.get(i));
            }
            return evaledNode;
        } finally {
            env.popScope();
        }
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
