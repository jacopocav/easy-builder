package com.github.jacopocav.builder.annotation;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface GeneratedBuilder {
    String className();

    String factoryMethodName();

    String setterPrefix();

    String buildMethodName();

    CopyFactoryMethodGeneration copyFactoryMethod();

    String copyFactoryMethodName();
}
