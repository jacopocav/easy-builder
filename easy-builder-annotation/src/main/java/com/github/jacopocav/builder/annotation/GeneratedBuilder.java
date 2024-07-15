package com.github.jacopocav.builder.annotation;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Effective options used for the annotated generated builder class
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface GeneratedBuilder {
    /**
     * The class of objects produced by this builder
     */
    Class<?> targetClass();

    /**
     * @see Builder#className()
     */
    String className();

    /**
     * @see Builder#factoryMethodName()
     */
    String factoryMethodName();

    /**
     * @see Builder#setterPrefix()
     */
    String setterPrefix();

    /**
     * @see Builder#buildMethodName()
     */
    String buildMethodName();

    /**
     * @see Builder#copyFactoryMethod()
     */
    CopyFactoryMethodGeneration copyFactoryMethod();

    /**
     * @see Builder#copyFactoryMethodName()
     */
    String copyFactoryMethodName();
}
