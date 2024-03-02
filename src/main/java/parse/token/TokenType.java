package parse.token;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;


public sealed interface TokenType {

    enum Lexical implements TokenType {
        LEFT_PAREN("("),
        RIGHT_PAREN(")"),
        LEFT_BRACE("{"),
        RIGHT_BRACE("}"),
        LEFT_BRACKET("["),
        RIGHT_BRACKET("]"),
        COMMA(","),
        BACK_SLASH("\\"),
        SINGLE_QUOTE("'"),
        QUOTE("\""),
        EOF(null);

        public final String stringValue;

        Lexical(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Syntactic implements TokenType {
        DOT("."),
        AMPERSAND("&"),
        GRAVE("`"),
        COLON(":"),
        SEMI_COLON(";"),
        TYPE("::"),
        POUND("#"),
        CACHE("$"),
        AT("@"),
        BAR("|"),
        TILDE("~"),
        SUPER("super"),
        THIS("this"),
        EQUAL("="),
        ELSE("else");

        public final String stringValue;

        Syntactic(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Operation implements TokenType {
        PLUS("+"),
        MINUS("-"),
        ASTERISK("*"),
        SLASH("/"),
        CARET("^"),
        PERCENT("%"),
        EQUALS("equals"),
        BANG_EQUAL("!="),
        REF_EQUALS("=="),

        GREATER(">"),
        LESS("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        PLUS_PLUS("++"),
        MINUS_MINUS("--"),

        AND("and"),
        OR("or"),
        NEGATE("!"),
        XOR("xor"),
        NAND("nand");

        public final String stringValue;

        Operation(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Literal implements TokenType {
        TRUE("#t"),
        FALSE("#f"),
        STRING(null),
        INT(null),
        LONG(null),
        FLOAT(null),
        DOUBLE(null),
        IDENTIFIER(null),
        NULL("null");

        public final String stringValue;

        Literal(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Expression implements TokenType {
        ASSIGN(":="),
        IF("if"),

        COND("cond"),
        PRINT("print"),
        BEGIN("begin"),
        FOR_I("for-i"),
        FOR_EACH("for-each"),
        WHILE("while"),
        CONS("cons"),
        CAR("car"),
        CAAR("caar"),
        CADR("cadr"),
        CDR("cdr"),
        CDDR("cddr"),
        CDAR("cdar");

        public final String stringValue;

        Expression(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Definition implements TokenType {
        DEFINE("define"),
        FUNC("func"),
        LAMBDA("lambda");;
        public final String stringValue;

        Definition(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    enum Modifier implements TokenType {
        MUTABLE("&mut"),
        MUTABLE_ALL("&mut-all"),
        FINAL("&fin"),
        FINAL_ALL("&fin-all"),
        VOLATILE("&vol"),
        VOLATILE_ALL("&vol-all"),
        PRIVATE("&priv"),
        PRIVATE_ALL("&priv-all"),
        PUBLIC("&pub"),
        PUBLIC_ALL("&pub-all"),
        PROTECTED("&prot"),
        PROTECTED_ALL("&prot-all"),
        STATIC("&stat"),
        STATIC_ALL("&stat-all"),
        SYNCHRONIZED("&sync"),
        SYNCHRONIZED_ALL("&sync-all"),
        CLOSURE_CLONE("&clos-clone"),
        OPTIONAL("&opt"),
        REST("&rest");

        public final String stringValue;


        Modifier(String stringValue) { this.stringValue = stringValue; }

        @Override
        public String asString() {
            return stringValue;
        }
    }

    // Utility

    String asString();

    record SingleToken(char chr, TokenType tokenType) { }

    record DualTokens(char chr1, char chr2, TokenType tokenType) { }

    record KeyWordToken(String keyword, TokenType tokenType) { }

    record ModifierToken(String modifierLexeme,TokenType tokenType) {}

    private static Stream<TokenType> getAllStream() {
        return Stream.of(
                Arrays.stream(Lexical.values()),
                Arrays.stream(Syntactic.values()),
                Arrays.stream(Operation.values()),
                Arrays.stream(Literal.values()),
                Arrays.stream(Expression.values()),
                Arrays.stream(Definition.values())
        ).flatMap(Function.identity());

    }

    static SingleToken[] getSingleTokens() {
        return getAllStream().filter(t -> t.asString() != null && t.asString().length() == 1)
                .map(t -> new SingleToken(t.asString().charAt(0), t))
                .toArray(SingleToken[]::new);
    }

    static DualTokens[] getDualTokens() {
        return getAllStream()
                .filter(t -> t.asString() != null && t.asString().length() == 2)
                .map(t -> new DualTokens(t.asString().charAt(0), t.asString().charAt(1), t))
                .toArray(DualTokens[]::new);
    }

    static KeyWordToken[] getKeyWordTokens() {
        return getAllStream()
                .filter(t -> t.asString() != null && t.asString().length() > 2)
                .map(t -> new KeyWordToken(t.asString(), t))
                .toArray(KeyWordToken[]::new);

    }

    static ModifierToken[] getModifierTokens() {
        return Arrays.stream(Modifier.values())
                .map(t -> new ModifierToken(t.asString(), t))
                .toArray(ModifierToken[]::new);
    }

}




