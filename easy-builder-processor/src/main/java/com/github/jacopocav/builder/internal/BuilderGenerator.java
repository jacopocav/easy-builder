package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.option.RawOptions;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import javax.lang.model.element.ExecutableElement;

public interface BuilderGenerator {
    /**
     * Generates a single builder class
     *
     * @param factory constructor or static factory method used to build target type instances
     * @return the declaration of a new builder class
     */
    GeneratedJavaFile generate(RawOptions rawOptions, ExecutableElement factory);
}
