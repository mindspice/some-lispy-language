import org.testng.annotations.Test;
import parse.node.LiteralNode;
import parse.node.Node;
import parse.token.Token;


public class ParseTest {



    public sealed interface Token permits T1, T2{

    }
    public enum  T1 implements Token {
        T1FIRST,
        T1SECOND
    }

    public enum T2 implements Token {
        T2FIRST,
        T2SECOND
    }



    public record Rec (int value, Token token) {}


    @Test
    void TestTest() {
        var rec = new Rec(1, T1.T1SECOND);
        System.out.println(rec.token);

        switch (rec.token) {
            case T1 t1 -> { }
            case T2 t2 -> { }
        }

    }
}
