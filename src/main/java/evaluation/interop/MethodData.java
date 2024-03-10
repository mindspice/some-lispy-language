package evaluation.interop;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public record MethodData(MethodHandle handle, Class<?> returnType, Class<?>[] paramTypes) {
    public boolean match(Class<?> rtn, Class<?>[] args) {
        if (rtn != null && returnType != rtn) {
            return false;
        }
        if (args.length != paramTypes.length) {
            return false;
        }

        // TODO add fuzzy matching
        for (int i = 0; i < paramTypes.length; ++i) {
            if (!paramTypes[i].isAssignableFrom(args[i]) && paramTypes[i] != Object.class) {
                System.out.println(paramTypes[i]);
                System.out.println(args[i]);
                return false;
            }
        }
        return true;
    }
}
