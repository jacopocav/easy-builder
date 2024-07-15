package com.github.jacopocav.builder.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

public class TypeUtils {
    private static final Type[] NO_TYPES = new Type[0];

    public static Class<?> getClass(Type type) {
        // getClass(MyType.class) == MyType.class
        if (Objects.requireNonNull(type) instanceof Class<?> cls) {
            return cls;
        }
        // getClass(MyType<Generic>) == MyType.class
        if (type instanceof ParameterizedType pt) {
            return getClass(pt.getRawType());
        }
        // getClass(? super MyType) == MyType.class
        if (type instanceof WildcardType wt && wt.getLowerBounds().length == 1) {
            return getClass(wt.getLowerBounds()[0]);
        }
        // getClass(? extends MyType) == MyType.class
        // getClass(?) == Object.class (equivalent to "? extends Object")
        if (type instanceof WildcardType wt && wt.getUpperBounds().length == 1) {
            return getClass(wt.getUpperBounds()[0]);
        }
        // getClass(T extends MyType) == MyType.class
        if (type instanceof TypeVariable<?> tv && tv.getBounds().length == 1) {
            return getClass(tv.getBounds()[0]);
        }
        throw new IllegalArgumentException("cannot determine raw class of type %s".formatted(type.getTypeName()));
    }

    public static Type[] getTypeArguments(Type type) {
        if (type instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        }

        return NO_TYPES;
    }
}
