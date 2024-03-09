package evaluation;

import language.types.data.Pair;
import parse.node.ExpressionNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class InterOp {
    private static final Map<String, Class<?>> classMap = new HashMap<>();

    private static final Set<Class<?>> ALLOWED_FLOAT_CONV = Set.of(Float.class, Double.class);
    private static final Set<Class<?>> ALLOWED_LONG_CONV = Set.of(Long.class, Float.class, Double.class);
    private static final Set<Class<?>> ALLOWED_INT_CONV = Set.of(Integer.class, Long.class, Float.class, Double.class);
    // TODO add the rest of implicits

    private static boolean canAssignFrom(Class<?> param, Object arg) {

        if (param == int.class) { return ALLOWED_INT_CONV.contains(arg.getClass()); }
        if (param == double.class) { return arg.getClass() == Double.class; }
        if (param == long.class) { return ALLOWED_LONG_CONV.contains(Long.class); }
        if (param == float.class) { return ALLOWED_FLOAT_CONV.contains(Float.class); }
        return param.isAssignableFrom(arg.getClass());
    }

    public static Object getClassInstance(String className, Object[] args) {
        try {
            Class<?> clazz = classMap.get(className);
            if (clazz == null) {
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Class not found: " + className);
                }
            }

            Constructor<?>[] constructors = clazz.getConstructors();

            for (int i = 0; i < constructors.length; ++i) {
                Class<?>[] paramTypes = constructors[i].getParameterTypes();
                if (paramTypes.length != args.length) { continue; }

                boolean match = true;
                for (int j = 0; j < paramTypes.length; j++) {
                    System.out.println("ParamClass: " + paramTypes[j]);
                    System.out.println("ArgClass: " + args[j].getClass());
                    if (!paramTypes[j].isInstance(args[j]) && !canAssignFrom(paramTypes[j], args[j])) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    try {
                        return constructors[i].newInstance(args);
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        throw new IllegalStateException("Failed to initialize" + className + " Error: " + e.getMessage());
                    }
                }
            }
            throw new IllegalStateException("No suitable constructor found for class: " + className + ", args:" + Arrays.toString(args));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
