import com.esotericsoftware.reflectasm.MethodAccess;
import org.testng.annotations.Test;
import parse.node.EvalResult;
import parse.node.LiteralNode;
import parse.node.ResultType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;


public class SandBox {

    @Test
    public void test() throws Throwable {
        System.out.println(int.class.isAssignableFrom(Integer.class));

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodType mtype = MethodType.methodType(List.class, int.class, int.class);

        var a = List.of(1, 2, 3, 4);
        var s = lookup.findVirtual(getLookupClass(new LiteralNode.ObjectLit(a)), "subList", mtype);
        System.out.println(s);

        var t = System.nanoTime();
        var types = Arrays.asList(List.class.getMethods());
        for (var type : types) {
            System.out.println(type.getName());
            System.out.println(type.getReturnType());
            System.out.println(Arrays.toString(type.getParameterTypes()));
            System.out.println("-----------------------");
        }
        System.out.println(System.nanoTime() - t);
    }



    private Class<?> getLookupClass(EvalResult object) {
        if (object.resultType() == ResultType.OBJECT) {
            if (object.asObject() instanceof Collection<?> col) {
                if (col instanceof List<?>) { return List.class; }
                if (col instanceof Map<?, ?>) { return Map.class; }
                if (col instanceof Set<?>) { return Set.class; }
                if (col instanceof Queue<?>) { return Queue.class; }
                return Collection.class;
            }
            if (object.asObject() instanceof Number number) {
                Class<?> clazz = object.asObject().getClass();
                if (clazz == Integer.class) { return int.class; }
                if (clazz == Double.class) { return double.class; }
                if (clazz == Boolean.class) { return boolean.class; }
                if (clazz == Long.class) { return long.class; }
                if (clazz == Float.class) { return float.class; }
                if (clazz == Short.class) { return short.class; }
                if (clazz == Byte.class) {
                    return byte.class;
                }
            }
        }
        return object.classType();
    }

}
