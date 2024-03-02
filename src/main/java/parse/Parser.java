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
        String error = String.format("[Line: %d] Error: %s", currToken.line(), msg);
        return new IllegalStateException(error);
    };

    public Node parse() {

        while (haveNext()) {
            switch (peek().type()) {
                case TokenType.Lexical lexical -> { }
                case TokenType.Definition definition -> { }
                case TokenType.Expression expression -> { }
                case TokenType.Literal literal -> { }
                case TokenType.Operation operation -> { }
                case TokenType.Syntactic syntactic -> { }
                case TokenType.Modifier modifier -> { }
                default -> onError("Unexpected value");
            }
        }

    }

    private void parseLexical(TokenType.Lexical lexical) {
        switch (lexical) {
            case LEFT_PAREN -> {
                depth++;
                advance();
            }
            case RIGHT_PAREN -> {
                depth--;
                advance();
            }
            case LEFT_BRACE -> { }
            case RIGHT_BRACE -> { }
            case LEFT_BRACKET -> { }
            case RIGHT_BRACKET -> { }
            case COMMA -> { }
            case SLASH -> { }
            case BACK_SLASH -> { }
            case SINGLE_QUOTE -> { }
            case QUOTE -> { }
            case EOF -> { }
        }
    }

    private Node parseDefine() {
        consume(TokenType.Definition.DEFINE, "Expected definition, found" + peek().type());
        String name = consume(TokenType.Literal.IDENTIFIER, "Definition without name").literal().toString();

        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;

        String varType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;

        if (peek().type() == TokenType.Lexical.LEFT_PAREN) {
            consumeLParen(""); // Consume opening paren
            if (peekN(2).type() == TokenType.Definition.LAMBDA) {
                DefinitionNode.LambdaDef lambda = parseLambda();
                return new DefinitionNode.FunctionDef(name, lambda);
            } else {
                return parseExpression();
            }
        } else if (match(TokenType.Literal.values())) {
            return new DefinitionNode.VariableDef(name, modifiers, varType, parseLiteral());
        }
        throw (onError.apply("Unexpected syntax after define"));
    }

    private DefinitionNode.LambdaDef parseLambda() {
        consume(TokenType.Definition.LAMBDA, "Expected lambda, found:" + peek().type());
        List<TokenType.Modifier> modifiers = match(TokenType.Modifier.values()) ? parseModifiers() : null;

        consumeLParen("Expected parameter list, found:" + peek().type());
        List<DefinitionNode.ParamDef> parameters = match(TokenType.Lexical.RIGHT_PAREN) ? null : parseParameters();

        consumeLParen("Expected body expression, found:" + peek().type());
        Node body = parseExpression();

        String returnType = match(TokenType.Syntactic.TYPE) ? advance().literal().toString() : null;
        return new DefinitionNode.LambdaDef(modifiers, parameters, body, returnType);
    }

    private Node parseExpression() {
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN) {
            Token token = peek();
            // Recurse nested expressions, this is needed for java interop and object method/field calls
            if (token.type() == TokenType.Lexical.LEFT_PAREN) {
                consumeLParen("");
                parseExpression();
            } else {
                switch (token.type()) {
                    // TODO add case for inline arrays/lists
                    case TokenType.Expression expression -> { }
                    case TokenType.Operation operation -> {

                    }
                    case TokenType.Literal literal -> { }
                }
            }
        }
    }

    private OperationNode parseOperation(TokenType.Operation operation) {
        consume(operation, "Expected operation, found:" + peek().type());
        List<Node> operands = new ArrayList<>(3);
        while (peek().type() != TokenType.Lexical.RIGHT_PAREN && haveNext()) {
            operands.add(parseExpression());
        }
        consumerRParen("Expected closing paren (EOF error?");
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

        consumerRParen("Error parsing parameters");
        return params;
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
            return ++depth;
        }
        throw (onError.apply(error));
    }

    private int consumerRParen(String error) {
        if (peek().type() == TokenType.Lexical.RIGHT_PAREN) {
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
