package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

public sealed interface Options permits RawOptions, InterpolatedOptions {
    String className();

    String setterPrefix();

    String buildMethodName();

    String staticFactoryName();

    CopyFactoryMethodGeneration copyFactoryMethod();

    String copyFactoryMethodName();
}
