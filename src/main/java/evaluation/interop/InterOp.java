package evaluation.interop;

import language.types.data.Pair;
import parse.node.EvalResult;
import parse.node.ExpressionNode;
import parse.node.ResultType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class InterOp {
    private static final Map<String, ClassData> classNameMap = new HashMap<>(20);
    private static final Map<Class<?>, ClassData> classMap = new HashMap<>(20);
    private static MethodHandles.Lookup lookup = MethodHandles.lookup();


    private static ClassData getClassData(String className) {
        ClassData data = classNameMap.get(className);
        if (data == null) {
            try {
                Class<?> clazz = Class.forName(className);
                data = ClassData.ofClass(clazz);
                classNameMap.put(className, data);
                classMap.put(clazz, data);
                return data;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to find class: " + className);
            }
        }
        return data;
    }

    private static ClassData getClassData(Class<?> clazz) {
        ClassData data = classMap.get(clazz);
        if (data == null) {

            data = ClassData.ofClass(clazz);
            classNameMap.put(clazz.getSimpleName(), data);
            classMap.put(clazz, data);
            return data;
        }
        return data;
    }

    private static final Set<Class<?>> ALLOWED_FLOAT_CONV = Set.of(Float.class, Double.class);
    private static final Set<Class<?>> ALLOWED_LONG_CONV = Set.of(Long.class, Float.class, Double.class);
    private static final Set<Class<?>> ALLOWED_INT_CONV = Set.of(Integer.class, Long.class, Float.class, Double.class, int.class);
    // TODO add the rest of implicits

    private static boolean canAssignFrom(Class<?> param, Object arg) {

        if (param == int.class) { return ALLOWED_INT_CONV.contains(arg.getClass()); }
        if (param == double.class) { return arg.getClass() == Double.class; }
        if (param == long.class) { return ALLOWED_LONG_CONV.contains(Long.class); }
        if (param == float.class) { return ALLOWED_FLOAT_CONV.contains(Float.class); }
        return param.isAssignableFrom(arg.getClass());
    }

    public static Object getFieldData(VarHandle handle, Object instance) {
        return handle.get(instance);
    }

    public static void setFieldData(VarHandle handle, Object instance, Object data) {
        handle.set(instance, data);
    }

    public Object invokeMethod(MethodHandle handle, Object instance, List<EvalResult> arguments) {
        Object[] finalArgs = new Object[arguments.size() + 1];
        finalArgs[0] = instance;

        for (int i = 0; i < arguments.size(); ++i) {
            finalArgs[i + 1] = arguments.get(i).asObject();
        }
        try {
            return handle.invoke(finalArgs);
        } catch (Throwable e) {
            throw new IllegalStateException("Method Invocation: " + handle.toString() + " threw:  " + e.getMessage());
        }
    }

    public static Class<?> getTypeClass(String typeName) {
        ClassData classData = getClassData(typeName);
        return classData.classRef();
    }

    public static VarHandle getField(String className, String fieldName, Class<?> type, boolean isStatic) {
        return getField(getClassData(className), fieldName, type, isStatic);
    }

    public static VarHandle getField(Class<?> clazz, String fieldName, Class<?> type, boolean isStatic) {
        return getField(getClassData(clazz), fieldName, type, isStatic);
    }

    public static VarHandle getField(ClassData classData, String fieldName, Class<?> type, boolean isStatic) {
        VarHandle handle = classData.getField(fieldName);
        if (handle != null) { return handle; }

        if (type != null) {
            try {
                handle = isStatic
                         ? lookup.findStaticVarHandle(classData.classRef(), fieldName, type)
                         : lookup.findVarHandle(classData.classRef(), fieldName, type);
                classData.addField(fieldName, handle);
                return handle;
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }
        }

        try {
            Field field = classData.classRef().getField(fieldName);
            handle = lookup.unreflectVarHandle(field);
            classData.addField(fieldName, handle);
            return handle;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to find field: " + fieldName + " in class: " + classData.classRef().getSimpleName());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Forbidden to access field: " + fieldName + " in class: " + classData.classRef().getSimpleName());
        }
    }

    public static MethodHandle getMethod(Class<?> clazz, String methodName, Class<?> rtnType, List<EvalResult> args, boolean isStatic) {
        return getMethod(getClassData(clazz), methodName, rtnType, args, isStatic);
    }

    public static MethodHandle getMethod(String className, String methodName, Class<?> rtnType, List<EvalResult> args, boolean isStatic) {
        return getMethod(getClassData(className), methodName, rtnType, args, isStatic);
    }

    public static MethodHandle getMethod(ClassData classData,
            String methodName,
            Class<?> rtnType,
            List<EvalResult> args,
            boolean isStatic) {
        List<Class<?>> paramTypes = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); ++i) {
            paramTypes.set(i, getLookupClass(args.get(i)));
        }
        MethodType methodTypes = MethodType.methodType(rtnType, paramTypes);

        MethodData data = classData.getMethod(methodName, rtnType, paramTypes);
        if (data != null) { return data.handle(); }

        MethodHandle handle = null;
        if (rtnType != null) {
            handle = getDirectMethodHandle(classData.classRef(), methodName, methodTypes, isStatic);
            if (handle != null) {
                classData.addMethod(methodName, handle);
                return handle;
            }
        }

        handle = searchForMethodHandle(classData.classRef(), methodName, methodTypes);
        if (handle != null) {
            classData.addMethod(methodName, handle);
            return handle;
        }
        throw new IllegalStateException("Failed to find method: " + methodName + " in class: " + classData.classRef().getSimpleName());
    }

    public static MethodHandle getDirectMethodHandle(Class<?> clazz, String methodName, MethodType methodType, boolean isStatic) {
        try {
            return isStatic
                   ? lookup.findStatic(clazz, methodName, methodType)
                   : lookup.findVirtual(clazz, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    public static MethodHandle searchForMethodHandle(Class<?> clazz, String methodName, MethodType methodType) {
        Method[] methods = clazz.getMethods();
        Class<?>[] paramTypes = methodType.parameterArray();

        next:
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            Class<?>[] methodParams = method.getParameterTypes();
            if (method.getParameterTypes().length != paramTypes.length) {
                continue;
            }

            for (int j = 0; j < methodParams.length; ++j) {
                if (!methodParams[j].isAssignableFrom(paramTypes[i])) {
                    break next;
                }
            }
            try {
                return lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Illegal method access: " + e.getMessage());
            }
        }
        return null;
    }

    public static Object getClassInstance(String className, Object[] args) {
        try {
            ClassData classData = getClassData(className);
            Constructor<?>[] constructors = classData.classRef().getConstructors();

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

    private static Class<?> getLookupClass(EvalResult object) {
        if (object.resultType() == ResultType.OBJECT) {
            if (object.asObject() instanceof Collection<?> col) {
                if (col instanceof List<?>) { return List.class; }
                if (col instanceof Map<?, ?>) { return Map.class; }
                if (col instanceof Set<?>) { return Set.class; }
                if (col instanceof Queue<?>) { return Queue.class; }
                return Collection.class;
            }
        }
        return object.classType();
    }
}
