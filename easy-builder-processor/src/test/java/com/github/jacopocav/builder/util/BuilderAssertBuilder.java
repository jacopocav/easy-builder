package com.github.jacopocav.builder.util;

public class BuilderAssertBuilder {
    private final Class<?> builderClass;

    BuilderAssertBuilder(Class<?> builderClass) {
        this.builderClass = builderClass;
    }

    /**
     * Sets the source class (i.e. the class of objects built by the builder).
     *
     * @param sourceClass the source class
     */
    public BuilderAssert withSourceClass(Class<?> sourceClass) {
        return new BuilderAssert(builderClass, sourceClass);
    }
}
