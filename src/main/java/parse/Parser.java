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
            consumeLParen("Expected start of  expression, found: " + peek().type());
            switch (peek().type()) {
                case TokenType.Definition definition -> {
                    switch (definition) {
                        case DEFINE -> topLevelExpressions.add(parseDefine());
                        case FUNC -> throw onError.apply("Unsupported top level operation: " + peek().type());
                        case LAMBDA -> throw onError.apply("Unsupported top level operation: " + peek().type());
                    }
                }
                case TokenType.Expression expression -> topLevelExpressions.add(parseGeneralExpression());
                case TokenType.Lexical lexical -> throw onError.apply("Unsupported top level operation: " + peek().type());
                case TokenType.Literal literal -> throw onError.apply("Unsupported top level operation: " + peek().type());
                case TokenType.Modifier modifier -> throw onError.apply("Unsupported top level operation: " + peek().type());
                case TokenType.Operation operation -> topLevelExpressions.add(parseOperation((TokenType.Operation) peek().type()));
                case TokenType.Syntactic syntactic -> throw onError.apply("Unsupported top level operation: " + peek().type());
            }

        }
        return new Node.program(topLevelExpressions);
    }

    private Node parseDefine() {
        consume(TokenType.Definition.DEFINE, "Expected definition, found" + peek().type());

        String name = consume(TokenType.Literal.IDENTIFIER, "Definition without name").literal().toString();
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;
        String varType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;

        Node definitionNode = null;

        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            consumeLParen(""); // Consume opening paren
            if (peekN(2).type() == TokenType.Definition.LAMBDA) {
                DefinitionNode.LambdaDef lambda = parseLambda();
                definitionNode = new DefinitionNode.FunctionDef(name, lambda);
            } else {
                definitionNode = parseGeneralExpression();
            }
        } else if (match(TokenType.Literal.values())) {
            definitionNode = new DefinitionNode.VariableDef(name, modifiers, varType, parseLiteral());
        }
        //consumeRParen("Unexpected syntax after define: " + peek().type());
        if (definitionNode == null) {
            throw onError.apply("Invalid syntax in define: " + peek().type());
        }
        return definitionNode;
    }

    private DefinitionNode.LambdaDef parseLambda() {
        consume(TokenType.Definition.LAMBDA, "Expected lambda, found:" + peek().type());
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;

        consumeLParen("Expected parameter list, found:" + peek().type());
        List<DefinitionNode.ParamDef> parameters = match(TokenType.Lexical.RIGHT_PAREN) ? null : parseParameters();

        consumeLParen("Expected body expression, found:" + peek().type());
        Node body = parseGeneralExpression();

        String returnType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;
        return new DefinitionNode.LambdaDef(modifiers, parameters, body, returnType);
    }

    private Node parseGeneralExpression() {
        Node exprNode = null;

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            Token token = peek();
            // Recurse nested expressions
            if (token.type() == TokenType.Lexical.LEFT_PAREN) {
                consumeLParen("");
                parseGeneralExpression();
            } else {
                switch (token.type()) {
                    // TODO add case for inline arrays/lists
                    case TokenType.Expression expression -> {
                        exprNode = parseExactExpression((TokenType.Expression) token.type());
                    }
                    case TokenType.Operation operation -> {
                        exprNode = parseOperation((TokenType.Operation) token.type());
                    }
                    case TokenType.Literal literal -> {
                        return parseLiteral(); // Directly return literals
                    }
                    default -> throw onError.apply("Unsupported operation: " + peek().type());
                }
            }
        }
        System.out.println(peek().type());
        //consumeRParen("Expected closing parentheses");
        if (exprNode == null) {
            throw onError.apply("No expression found");
        }
        return exprNode;
    }

    private ExpressionNode parseExactExpression(TokenType.Expression expression) {
        consume(expression, "Expected expression, found:" + peek().type());
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
            default -> throw onError.apply("Unsupported operation: " + peek().type());
        }
    }

    private ExpressionNode parseAssign() {
        consume(TokenType.Expression.ASSIGN, "Expected assignment symbol");
        String identifier = consume(TokenType.Literal.IDENTIFIER, "Expected identifier for assignment").literal().toString();
        Node value = parseGeneralExpression();
        return new ExpressionNode.AssignOp(identifier, value);
    }

    private ExpressionNode parseIf() {
        consume(TokenType.Expression.ASSIGN, "Expected if symbol");
        Node condition = parseGeneralExpression();
        Node thenBranch = parseGeneralExpression();
        ExpressionNode.CondBranch condBranch = new ExpressionNode.CondBranch(condition, thenBranch);
        Node elseBranch = peek().type() != TokenType.Lexical.RIGHT_PAREN ? parseGeneralExpression() : null;
        return new ExpressionNode.IfExpr(condBranch, elseBranch);
    }

    private ExpressionNode.CondBranch parseCondBranch() {
        Node condition = parseGeneralExpression();
        Node thenBranch = parseGeneralExpression();
        return new ExpressionNode.CondBranch(condition, thenBranch);
    }

    private ExpressionNode parseCond() {
        consume(TokenType.Expression.COND, "Expected cond symbol");

        List<ExpressionNode.CondBranch> condBranches = new ArrayList<>(5);
        Node elseBranch = null;

        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            if (peekN(2).type() == TokenType.Syntactic.ELSE) {
                consumeLParen("Expected opening parenthesis for else");
                consume(TokenType.Syntactic.ELSE, "Expected else symbol");
                elseBranch = parseGeneralExpression();
                break;
            }
            consumeLParen("Expected enclosing parenthesis for cond branch");
            condBranches.add(parseCondBranch());
        }
        consumeRParen("Expected closing parenthesis");
        return new ExpressionNode.CondExpr(condBranches, elseBranch);
    }

    private OperationNode parseOperation(TokenType.Operation operation) {
        consume(operation, "Expected operation, found:" + peek().type());

        List<Node> operands = new ArrayList<>(3);
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            operands.add(parseGeneralExpression());
        }
        consumeRParen("Expected closing paren (EOF error?");

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

    private List<DefinitionNode.ParamDef> parseParameters() {
        List<DefinitionNode.ParamDef> params = new ArrayList<>(3);

        boolean optional = false;
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            if (peek().type() == TokenType.Modifier.OPTIONAL) {
                optional = true;
                advance();
            }

            String name = consume(TokenType.Literal.IDENTIFIER, "Parameter Identifier expected").literal().toString();
            LiteralNode value = null;
            String type = null;

            if (optional) {
                if (peek().type() == TokenType.Syntactic.EQUAL) {
                    advance(); // skip = token it is just syntactical
                    value = parseLiteral();
                }
            }

            if (peek().type() == TokenType.Syntactic.TYPE) {
                type = advance().literal().toString();
            }
            params.add(new DefinitionNode.ParamDef(name, type, optional, value));
        }

        consumeRParen("Error parsing parameters");
        return params;
    }

    private LiteralNode parseLiteral() {
        Token token = advance();
        System.out.println("Literal Token: " + token);
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
}
