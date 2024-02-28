package parse.token;

public record Token(
        TokenType type,
        String lexeme,
        Object literal,
        int line
) { }
