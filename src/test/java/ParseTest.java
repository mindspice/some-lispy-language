import org.testng.annotations.Test;
import parse.Lexer;
import parse.Parser;


public class ParseTest {




    @Test
    void RandomTest() {

        String s = """
                (cond
                  ((< x 0) 10)
                  ((> x 0) 20)
                  ((== x 0) 0)
                  (#t (* 10 20))
                  )
                """;
        var lex = new Lexer(s).start();
        var parse = new Parser(lex).start();
        System.out.println(parse);

    }
}
