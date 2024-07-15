package com.github.jacopocav.builder.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static java.util.Objects.hash;

/**
 * Represents a <em>reified</em> generic type T.
 *
 * @apiNote this class should be used by directly subclassing and
 * <b>explicitly passing the generic type argument</b> (otherwise it might not work). Example:
 * {@snippet :
 * import java.lang.reflect.Type;
 * import java.util.List;
 *
 * void ok() {
 *     // works fine
 *     var listOfIntegers = new GenericType<List<Integer>>() {};
 *     // generic type preserves the Integer type variable
 *     Type listOfIntegersType = listOfIntegers.getGenericType();
 *     // raw class is same as List.class
 *     Class<T> listType = listOfIntegers.getRawClass();
 * }
 *
 * <T> void notOk() {
 *     // will throw IllegalArgumentException because
 *     // T is a type variable, not a "concrete" type
 *     var t = new GenericType<T>() {};
 * }
 *}
 */
public abstract class GenericType<T> {
    private final Type genericType;
    private final Class<T> rawClass;

    protected GenericType() {
        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        genericType = genericSuperclass.getActualTypeArguments()[0];
        rawClass = computeRawClass(genericType);
    }

    public Class<T> getRawClass() {
        return rawClass;
    }

    public Type getGenericType() {
        return genericType;
    }

    @Override
    public int hashCode() {
        return hash(genericType, rawClass);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GenericType<?> that
               && Objects.equals(genericType, that.genericType)
               && Objects.equals(rawClass, that.rawClass);
    }

    @Override
    public String toString() {
        return "GenericType[genericType=%s, rawClass=%s]".formatted(genericType, rawClass.getName());
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> computeRawClass(Type genericType) {
        return switch (genericType) {
            case Class<?> cls -> (Class<T>) cls;
            case ParameterizedType parameterized -> computeRawClass(parameterized.getRawType());
            case GenericArrayType array -> (Class<T>)
                    computeRawClass(array.getGenericComponentType()).arrayType();
            default -> throw new IllegalArgumentException("cannot determine raw class for type " + genericType);
        };
    }
}
