package com.github.jacopocav.builder.internal.generation;

import javax.lang.model.element.Element;

public interface SingleElementJavaFileGenerator {
    /**
     * Generates a java source file from a single {@code element}.
     */
    GeneratedJavaFile generate(Element element);
}