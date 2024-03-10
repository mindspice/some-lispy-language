package evaluation.interop;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record ClassData(Class<?> classRef, Map<String, List<MethodData>> methodHandles, Map<String, VarHandle> varHandles) {
    public static ClassData ofClass(Class<?> clazz) {
        return new ClassData(clazz, new HashMap<>(), new HashMap<>());
    }

    // TODO uses some type of hashing and mapFlat with a more performant map implementation;
    public void addMethod(String name, MethodHandle handle) {
        var handles = methodHandles.computeIfAbsent(name, k -> new ArrayList<>(3));
        MethodData data = new MethodData(
                handle,
                handle.type().returnType(),
                handle.type().parameterArray()
        );
        handles.add(data);
    }

    public MethodData getMethod(String name, Class<?> rtnType, List<Class<?>> params) {
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


