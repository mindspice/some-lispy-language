
import static org.junit.Assert.*;

import org.testng.annotations.Test;
import parse.Lexer;
import parse.token.TokenType;


public class LexTest {

    @Test
    public void testNumberLexing() {
        var lex = new Lexer("(* 2000.00  100 200f 1l 99d) (!= 10");
        var tokens = lex.start();
        assertEquals(TokenType.DOUBLE, tokens.get(2).type());
        assertEquals(2000.00, tokens.get(2).literal());

        assertEquals(TokenType.INT, tokens.get(3).type());
        assertEquals(100, tokens.get(3).literal());

        assertEquals(TokenType.FLOAT, tokens.get(4).type());
        assertEquals(200.00f, tokens.get(4).literal());

        assertEquals(TokenType.LONG, tokens.get(5).type());
        assertEquals(1L, tokens.get(5).literal());

        assertEquals(TokenType.DOUBLE, tokens.get(6).type());
        assertEquals(99D, tokens.get(6).literal());
    }

    @Test
    public void testStringLexing() {
        var lex = new Lexer("(def x \"String Literal\")");
        var tokens = lex.start();
        assertEquals("String Literal", tokens.get(3).literal());

        var lex2 = new Lexer("(def x \"String\nLiteral\")");
        var tokens2 = lex2.start();
        assertEquals("String\nLiteral", tokens2.get(3).literal());

//        var lex3 = new Lexer("(def x \"String Literal\"with nested\" string\")");
//        var tokens3 = lex3.start();
//        tokens3.forEach(System.out::println);
//        assertEquals("String Literal\"with nested\" string", tokens3.get(3).literal());

    }

    @Test
    public void testKeywordAndIdentityLexing() {
        var lex = new Lexer("""
                (define x 10)
                (for-i 0 10 2)
                (TestFunction1)
                (testFunction2)
                (test-Function3)
                """);
        var tokens = lex.start();

        assertEquals(TokenType.DEFINE, tokens.get(1).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());

        assertEquals(TokenType.FOR_I, tokens.get(6).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(12).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(15).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(18).type());

    }

    @Test
    public void typeLexing() {
        var lex = new Lexer("(define x ::int 10)");
        var tokens = lex.start();
        tokens.forEach(t -> System.out.println(t.type() + " | " + t.literal()));
    }

}
