package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.internal.option.Options;

import javax.lang.model.element.ExecutableElement;

public interface BuilderGenerator {
    /**
     * Generates a single builder class
     *
     * @param factory constructor or static factory method used to build target type instances
     * @param options options for the builder
     * @return the declaration of a new builder class
     */
    GeneratedJavaFile generate(ExecutableElement factory, Options options);
}
