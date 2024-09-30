package com.github.jacopocav.builder.internal.type;

/**
 * Factory for {@link TypeRegistry}
 */
public class TypeRegistryFactory {
    public TypeRegistry create() {
        return new TypeRegistryImpl();
    }
}
