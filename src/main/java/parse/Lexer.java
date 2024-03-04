package parse;

import parse.token.Token;
import parse.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class Lexer {

    private String source;
    private List<Token> tokens;
    private int startIndex = 0;
    private int currIndex = 0;
    private int lineNum = 1;

    private final TokenType.SingleToken[] SINGLE_TOKENS;
    private final TokenType.DualTokens[] DUAL_TOKENS;
    private final TokenType.KeyWordToken[] KEYWORD_TOKENS;
    private final TokenType.ModifierToken[] MODIFIER_TOKENS;

    public Lexer() {

        SINGLE_TOKENS = TokenType.getSingleTokens();
        DUAL_TOKENS = TokenType.getDualTokens();
        KEYWORD_TOKENS = TokenType.getKeyWordTokens();
        MODIFIER_TOKENS = TokenType.getModifierTokens();
    }

    public List<Token> process(String source) {
        startIndex = 0;
        currIndex = 0;
        lineNum = 1;
        this.source = source;
        this.tokens = new ArrayList<>(source.length() / 5);

        while (haveNext()) {
            startIndex = currIndex;
            char currChar = advance();

            switch (currChar) {
                case ' ', '\r', '\t' -> { continue; }
                case '\n' -> {
                    lineNum++;
                    continue;
                }
            }

            if (lexDualToken(currChar)) { continue; }
            if (lexSingleToken(currChar)) { continue; }
            if (isNumeric(currChar)) {
                lexNumber();
                continue;
            }
            if (lexKeywordOrIdentifier()) { continue; }

            throw new IllegalStateException("Invalid formatting encountered on line: " + lineNum);
        }
        addToken(TokenType.Lexical.EOF);
        return tokens;
    }

    public TokenType matchSingle(char c) {
        for (var t : SINGLE_TOKENS) {
            if (t.chr() == c) {
                return t.tokenType();
            }
        }
        return null;
    }

    public TokenType matchDouble(char c1, char c2) {
        for (var t : DUAL_TOKENS) {
            if (t.chr1() == c1 && t.chr2() == c2) {
                return t.tokenType();
            }
        }
        return null;
    }

    public TokenType matchKeyWord(String word) {
        for (var t : KEYWORD_TOKENS) {
            if (t.keyword().equals(word)) {
                return t.tokenType();
            }
        }
        return null;
    }

    public TokenType matchModifier(String word) {
        for (var t : MODIFIER_TOKENS) {
            if (t.modifierLexeme().equals(word)) {
                return t.tokenType();
            }
        }
        return null;
    }

    private boolean lexSingleToken(char c) {
        TokenType singleToken = matchSingle(c);
        if (singleToken == null) { return false; }

        switch (singleToken) { //TODO add comments
            case TokenType.Syntactic.AMPERSAND -> lexModifier();
            case TokenType.Lexical.QUOTE -> {
                lexString();
                return true;
            }
            default -> addToken(singleToken);
        }
        return true;
    }

    private boolean lexDualToken(char c1) {
        if (!haveNext() || peekOne() == ' ') {
            return false;
        }
        TokenType dblToken = matchDouble(c1, peekOne());
        if (dblToken == null) { return false; }
        advance(); // consume 2nd char

        switch (dblToken) {
            case TokenType.Syntactic.TYPE -> lexType();
            default -> addToken(dblToken);
        }
        return true;
    }

    private boolean lexType() {
        while (!isDefEnd(peekOne()) && haveNext()) {
            advance();
        }
        addToken(TokenType.Syntactic.TYPE, source.substring(startIndex + 2, currIndex));
        return true;
    }

    private boolean lexModifier() {
        while (!isDefEnd(peekOne()) && haveNext()) {
            advance();
        }
        String text = source.substring(startIndex, currIndex);
        TokenType modToken = matchModifier(text);
        if (modToken == null) {
            throw new RuntimeException("Invalid syntax: " + text);
        }
        addToken(modToken, text);
        return true;
    }

    // TODO handled nested strings

    private void lexString() {
        char peeked;
        while ((peeked = peekOne()) != '"' && haveNext()) {
            if (peeked == '\n') { lineNum++; }
            advance();
        }

        if (!haveNext()) {
            throw new IllegalStateException("Unterminated string line: " + lineNum);
        }

        advance(); // Consume closing "

        String value = source.substring(startIndex + 1, currIndex - 1); // Trim outer " "
        addToken(TokenType.Literal.STRING, value);
    }

    public void lexNumber() {
        char litType = '\0';

        if (peekOne() == '.') {
            advance();
            litType = 'd';
        }

        while (isNumeric(peekOne())) {
            System.out.println(peekOne());
            advance();

            if (peekOne() == '.' && isNumeric(peekTwo())) {
                advance();
                litType = 'd';
            }
        }

        char nextPeek = peekOne();
        boolean skipLit = false;
        if (nextPeek == 'f' || nextPeek == 'F') {
            litType = 'f';
            numTermCheck.accept(peekTwo());
            skipLit = true;
        } else if (nextPeek == 'l' || nextPeek == 'L') {
            numTermCheck.accept(peekTwo());
            litType = 'l';
            skipLit = true;

        } else if (nextPeek == 'd' || nextPeek == 'D') {
            litType = 'd';
            numTermCheck.accept(peekTwo());
            skipLit = true;

        }

        switch (litType) {
            case 'd' -> addToken(TokenType.Literal.DOUBLE, Double.parseDouble(source.substring(startIndex, currIndex)));
            case 'f' -> addToken(TokenType.Literal.FLOAT, Float.parseFloat(source.substring(startIndex, currIndex)));
            case 'l' -> addToken(TokenType.Literal.LONG, Long.parseLong(source.substring(startIndex, currIndex)));
            default -> {
                long value = Long.parseLong(source.substring(startIndex, currIndex));
                if (value < Integer.MAX_VALUE) {
                    addToken(TokenType.Literal.INT, (int) value);
                } else {
                    addToken(TokenType.Literal.LONG, value);
                }
            }
        }
        if (skipLit) {
            advance();
        }
    }

    public boolean lexKeywordOrIdentifier() {
        while (isAlphaNumeric(peekOne())) {
            advance();
        }

        String text = source.substring(startIndex, currIndex);
        TokenType kwToken = matchKeyWord(text);
        addToken(Objects.requireNonNullElse(kwToken, TokenType.Literal.IDENTIFIER), text);
        return true;
    }

    private char advance() {
        return source.charAt(currIndex++);
    }

    private char peekOne() {
        return haveNext() ? source.charAt(currIndex) : '\0';
    }

    private char peekTwo() {
        return (currIndex < source.length()) ? source.charAt(currIndex + 1) : '\0';
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    public boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_'
                || c == '-';
    }

    private boolean isDefEnd(char c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == ')';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(startIndex, currIndex);
        tokens.add(new Token(type, text, literal, lineNum));
    }

    private boolean haveNext() {
        return currIndex != source.length();
    }

    //  Helpers
    private final char[] terminalChars = new char[]{' ', ')', '}', ']'};

    private boolean isTerminalChar(char c) {
        for (char t : terminalChars) {
            if (c == t) { return true; }
        }
        return false;
    }

    private final Consumer<Character> numTermCheck = (c) -> {
        if (c != ' ' && c != ')') {
            throw new IllegalStateException("Encountered data directly after numeric literal terminator on line: " + lineNum);
        }
    };

}
