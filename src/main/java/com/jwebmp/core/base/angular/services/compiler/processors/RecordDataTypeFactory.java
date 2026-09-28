package com.jwebmp.core.base.angular.services.compiler.processors;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Creates a record solely to access its type-driven TypeScript renderer. */
final class RecordDataTypeFactory {
    private RecordDataTypeFactory() {
    }

    static Object create(Class<?> type) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            parameterTypes[i] = components[i].getType();
            arguments[i] = emptyValue(parameterTypes[i]);
        }
        Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
        return constructor.newInstance(arguments);
    }

    private static Object emptyValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        if (type == String.class) return "";
        if (type == Optional.class) return Optional.empty();
        if (type == List.class || type == Collection.class) return List.of();
        if (type == Set.class) return Set.of();
        if (type == Map.class) return Map.of();
        if (type.isArray()) return Array.newInstance(type.getComponentType(), 0);
        return null;
    }
}
