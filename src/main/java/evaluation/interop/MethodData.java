package evaluation.interop;

import java.lang.invoke.MethodHandle;
import java.util.List;


public record MethodData(MethodHandle handle, Class<?> returnType, Class<?>[] paramTypes) {
    public boolean match(Class<?> rtn, List<Class<?>> args) {
        if (rtn != null && returnType != rtn) {
            return false;
        }
        if (args.size() != paramTypes.length) {
            return false;
        }

        // TODO add fuzzy matching
        for (int i = 0; i < paramTypes.length; ++i) {
            if (!paramTypes[i].isAssignableFrom(args.get(i))) {
                return false;
            }
        }
        return true;
    }
}
