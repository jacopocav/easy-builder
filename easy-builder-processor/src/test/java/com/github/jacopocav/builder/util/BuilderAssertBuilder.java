package com.github.jacopocav.builder.util;

public class BuilderAssertBuilder {
    private final Class<?> builderClass;

    BuilderAssertBuilder(Class<?> builderClass) {
        this.builderClass = builderClass;
    }

    /**
     * Sets the target class (i.e. the class of objects built by the builder).
     */
    public BuilderAssert withTargetClass(Class<?> targetClass) {
        return new BuilderAssert(builderClass, targetClass);
    }
}
