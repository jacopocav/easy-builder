package com.github.jacopocav.builder.internal.finder;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

@FunctionalInterface
public interface CreatorMethodFinder {
    /**
     * Finds the <em>creator method</em> (i.e. the constructor or static factory method that will
     * be used by the builder to build new instances of the target type).
     *
     * @param annotatedElement the element annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder}
     * @return the creator method
     */
    ExecutableElement find(Element annotatedElement);
}
