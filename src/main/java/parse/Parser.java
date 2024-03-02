package parse;

import parse.node.*;
import parse.token.Token;
import parse.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private int depth = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Function<String, IllegalStateException> onError = (String msg) -> {
        Token currToken = peek();
        String error = String.format("[Line: %d, Token: %d] Error: %s", currToken.line(), current, msg);
        return new IllegalStateException(error);
    };

    public Node start() {
        List<Node> topLevelExpressions = new ArrayList<>(100);

        while (haveNext()) {
            topLevelExpressions.add(parseSExpr());
//            switch (peek().type()) {
//                case TokenType.Definition definition -> {
//                    switch (definition) {
//                        case DEFINE -> topLevelExpressions.add(parseDefine());
//                        case FUNC -> throw onError.apply("Unsupported top level operation: " + peek().type());
//                        case LAMBDA -> throw onError.apply("Unsupported top level operation: " + peek().type());
//                    }
//                }
//                case TokenType.Expression expression -> topLevelExpressions.add(parseGeneralExpression());
//                case TokenType.Lexical lexical -> throw onError.apply("Unsupported top level operation: " + peek().type());
//                case TokenType.Literal literal -> throw onError.apply("Unsupported top level operation: " + peek().type());
//                case TokenType.Modifier modifier -> throw onError.apply("Unsupported top level operation: " + peek().type());
//                case TokenType.Operation operation -> topLevelExpressions.add(parseOperation((TokenType.Operation) peek().type()));
//                case TokenType.Syntactic syntactic -> throw onError.apply("Unsupported top level operation: " + peek().type());
            //  }

        }
        return new Node.program(topLevelExpressions);
    }

    private Node parseSExpr() {
        Node expression = null;
        consumeLParen("Expected start of s-expression: " + peek().literal());
        expression = parseExpressionData();
        consumeRParen("Expected closing parenthesis of expression, found: " + peek().type());

        if (expression == null) {
            throw onError.apply("Encountered unexpected syntax while parsing");
        }
        return expression;
    }

    private Node parseDefine() {
        consume(TokenType.Definition.DEFINE, "Expected definition, found: " + peek().type());

        String name = consume(TokenType.Literal.IDENTIFIER, "Definition without name").lexeme();
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;
        String varType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;

        DefinitionNode definitionNode = null;

        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {

            if (peekN(2).type() == TokenType.Definition.LAMBDA) {
                consumeLParen("Expected start of s-expr, found " + peek().type());
                DefinitionNode.LambdaDef lambda = parseLambda();
                definitionNode = new DefinitionNode.FunctionDef(name, lambda);
                consumeRParen("Expected end of s-expr, found: " + peek().type());

            } else {
                definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseExpressionData());
            }
        } else if (match(TokenType.Literal.values())) {
            definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseLiteral());
        }
        if (definitionNode == null) {
            throw onError.apply("Invalid syntax in define: " + peek().type());
        }

        if (definitionNode instanceof DefinitionNode.FunctionDef && varType != null) { // TODO implement actual warnings
            System.out.println(String.format("Type specifier %s, is unused. Variable type specification ignored on lambda bound variables.", varType));
        }

        return definitionNode;
    }

    private DefinitionNode.LambdaDef parseLambda() {
        consume(TokenType.Definition.LAMBDA, "Expected lambda, found:" + peek().type());
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;

        List<DefinitionNode.ParamDef> parameters = parseParameters();
        Node body = parseSExpr();

        String returnType = (peek().type() == TokenType.Syntactic.TYPE) ? advance().lexeme() : null;

        return new DefinitionNode.LambdaDef(modifiers, parameters, body, returnType);
    }

    private List<DefinitionNode.ParamDef> parseParameters() {

        consumeLParen("Expected opening parenthesis for parameters");
        List<DefinitionNode.ParamDef> params = new ArrayList<>(3);

        boolean optional = false;
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            if (peek().type() == TokenType.Modifier.OPTIONAL) {
                optional = true;
                advance();
            }

            printRemainingTokens();
            String name = consume(TokenType.Literal.IDENTIFIER, "Parameter Identifier expected").literal().toString();
            LiteralNode value = null;
            String type = null;

            if (peek().type() == TokenType.Syntactic.EQUAL) {
                if (!optional) {
                    throw onError.apply("Must specify &opt to declare optional parameters");
                }
                advance(); // skip = token it is just syntactical
                value = parseLiteral();
            }

            if (peek().type() == TokenType.Syntactic.TYPE) {
                type = advance().literal().toString();
            }
            params.add(new DefinitionNode.ParamDef(name, type, optional, value));
        }

        consumeRParen("Error parsing parameters");
        return params;
    }

    private Node parseExpressionData() {
        Token token = peek();
        // Recurse nested expressions
        if (token.type() == TokenType.Lexical.LEFT_PAREN) {
            return parseSExpr();
        }

        switch (token.type()) {
            // TODO add case for inline arrays/lists
            case TokenType.Definition.DEFINE -> {
                return parseDefine();
            }
            case TokenType.Definition.LAMBDA -> {
                return parseLambda();
            }
            case TokenType.Expression expression -> {
                return parseExactExpression((TokenType.Expression) token.type());
            }
            case TokenType.Operation operation -> {
                return parseOperation((TokenType.Operation) token.type());
            }
            case TokenType.Literal literal -> {
                return parseLiteral(); // Directly return literals
            }
            default -> throw onError.apply("Unexpected syntax in expression: " + peek().type());
        }

    }

    private ExpressionNode parseExactExpression(TokenType.Expression expression) {
        consume(expression, "Expected expression, found:" + peek().lexeme());
        switch (expression) {
            case ASSIGN -> { return parseAssign(); }
            case IF -> { return parseIf(); }
            case COND -> { return parseCond(); }
//            case PRINT -> { }
//            case FOR_I -> { }
//            case FOR_EACH -> { }
//            case WHILE -> { }
//            case CONS -> { }
//            case CAR -> { }
//            case CAAR -> { }
//            case CADR -> { }
//            case CDR -> { }
//            case CDDR -> { }
//            case CDAR -> { }
            default -> throw onError.apply("Unsupported operation: " + peek().lexeme());
        }
    }

    private ExpressionNode parseAssign() {
        consume(TokenType.Expression.ASSIGN, "Expected assignment symbol");
        String identifier = consume(TokenType.Literal.IDENTIFIER, "Expected identifier for assignment").lexeme();
        Node value = parseExpressionData();
        return new ExpressionNode.AssignOp(identifier, value);
    }

    private ExpressionNode parseIf() {
        Node condition = parseExpressionData();
        Node thenBranch = parseExpressionData();
        ExpressionNode.CondBranch condBranch = new ExpressionNode.CondBranch(condition, thenBranch);
        Node elseBranch = peek().type() != TokenType.Lexical.RIGHT_PAREN ? parseExpressionData() : null;
        return new ExpressionNode.IfExpr(condBranch, elseBranch);
    }

    private ExpressionNode.CondBranch parseCondBranch() {
        consumeLParen("Expected opening parenthesis conditional branch expression");
        Node condition = parseExpressionData();
        Node thenBranch = parseExpressionData();
        consumeRParen("Expected closing parenthesis conditional branch expression");
        return new ExpressionNode.CondBranch(condition, thenBranch);
    }

    private ExpressionNode parseCond() {
        List<ExpressionNode.CondBranch> condBranches = new ArrayList<>(5);
        Node elseBranch = null;

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            printRemainingTokens();
            if (peekN(2).type() == TokenType.Syntactic.ELSE) {
                consumeLParen("Expected opening parenthesis for else");
                consume(TokenType.Syntactic.ELSE, "Expected else symbol");
                elseBranch = parseExpressionData();
                consumeRParen("Expecting closing parenthesis for else");
                break;
            }
            condBranches.add(parseCondBranch());
        }
        return new ExpressionNode.CondExpr(condBranches, elseBranch);
    }

    private OperationNode parseOperation(TokenType.Operation operation) {
        consume(operation, "Expected operation, found:" + peek().type());

        List<Node> operands = new ArrayList<>(3);
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            operands.add(parseExpressionData());
        }

        if (operands.isEmpty()) {
            throw onError.apply("Operation must have arguments");
        }
        if (operation == TokenType.Operation.NEGATE && operands.size() > 1) {
            throw onError.apply("Unary operation can only have 1 argument");
        }

        switch (operation) {
            case PLUS, MINUS, ASTERISK, SLASH, CARET, PERCENT, PLUS_PLUS, MINUS_MINUS -> {
                return new OperationNode.ArithmeticOp(operation, operands);
            }
            case EQUALS, REF_EQUALS, BANG_EQUAL -> {
                return new OperationNode.EqualityOp(operation, operands);
            }
            case GREATER, LESS, GREATER_EQUAL, LESS_EQUAL -> {
                return new OperationNode.ComparisonOp(operation, operands);
            }
            case AND, OR, NAND, XOR -> {
                return new OperationNode.BooleanOp(operation, operands);
            }
            case NEGATE -> {
                return new OperationNode.UnaryOp(operation, operands.getFirst());
            }
            default -> throw (onError.apply("Fatal(Unrecognized operand"));
        }
    }

    private LiteralNode parseLiteral() {
        Token token = advance();
        if (token.type() instanceof TokenType.Literal literal) {
            return switch (literal) {
                case TRUE -> new LiteralNode.BooleanLit(true);
                case FALSE -> new LiteralNode.BooleanLit(false);
                case STRING -> new LiteralNode.StringLit(token.literal().toString());
                case INT -> new LiteralNode.IntLit((Integer) token.literal());
                case LONG -> new LiteralNode.LongLit((Long) token.literal());
                case FLOAT -> new LiteralNode.FloatLit((Float) token.literal());
                case DOUBLE -> new LiteralNode.DoubleLit((Double) token.literal());
                case IDENTIFIER -> new LiteralNode.ObjectLit(token.literal());
                case NULL -> new LiteralNode.NullLit();
            };
        }
        throw (onError.apply("Expected literal value"));
    }

    private List<TokenType.Modifier> parseModifiers() {
        List<TokenType.Modifier> modifiers = new ArrayList<>(3);
        while (match(TokenType.Modifier.values())) {
            modifiers.add((TokenType.Modifier) advance().type());
        }
        return modifiers;
    }

    private Token advance() {
        // debug
        if (peek().type() == TokenType.Lexical.LEFT_PAREN || peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            throw new RuntimeException("Parenthesis should only be advanced via consumeParen");
        }
        //

        if (haveNext()) { current++; }
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String error) {
        // DEBUG
        if (peek().type() == TokenType.Lexical.LEFT_PAREN || peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            throw new RuntimeException("Parenthesis should only be advanced via consumeParen");
        }
        //

        if (check(type)) {
            return advance();
        }
        throw (onError.apply(error));
    }

    private int consumeLParen(String error) {
        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            if (haveNext()) { current++; }
            return ++depth;
        }
        throw (onError.apply(error));
    }

    private int consumeRParen(String error) {
        if (peek().type() == TokenType.Lexical.RIGHT_PAREN) {
            if (haveNext()) { current++; }
            return --depth;
        }
        throw (onError.apply(error));
    }

    private boolean check(TokenType type) {
        if (!haveNext()) { return false; }
        return peek().type() == type;
    }

    private boolean haveNext() {
        return peek().type() != TokenType.Lexical.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekN(int n) {
        return tokens.get(current + (n - 1));
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void printRemainingTokens() {
        System.out.println("Remaining tokens: ");
        for (int i = current; i < tokens.size(); ++i) {
            System.out.print(tokens.get(i).type() + ", ");
        }
        System.out.println();
    }
}
