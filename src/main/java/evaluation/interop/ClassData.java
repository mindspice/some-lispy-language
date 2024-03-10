package evaluation.interop;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.*;


public record ClassData(Class<?> classRef, Map<String, List<MethodData>> methodHandles, Map<String, VarHandle> varHandles) {
    public static ClassData ofClass(Class<?> clazz) {
        return new ClassData(clazz, new HashMap<>(), new HashMap<>());
    }

    // TODO uses some type of hashing and mapFlat with a more performant map implementation;
    public void addMethod(String name, MethodHandle handle) {
        var handles = methodHandles.computeIfAbsent(name, k -> new ArrayList<>(3));
        Class<?>[] paramArray =  handle.type().parameterArray();
        MethodData data = new MethodData(
                handle,
                handle.type().returnType(),
               paramArray.length > 0 ? Arrays.copyOfRange(paramArray, 1 , paramArray.length) : paramArray
        );
        handles.add(data);
    }

    public MethodData getMethod(String name, Class<?> rtnType, Class<?>[] params) {
        var methods = methodHandles.get(name);
        if (methods == null) { return null; }

        for (int i = 0; i < methods.size(); ++i) {
            if (methods.get(i).match(rtnType, params)) {
                return methods.get(i);
            }
        }
        return null;
    }

    public void addField(String name, VarHandle handle) {
        varHandles.put(name, handle);
    }

    public VarHandle getField(String name) {
        return varHandles.get(name);
    }

}


