package com.github.jacopocav.builder.internal.finder.strategy;

import javax.lang.model.element.ExecutableElement;

/**
 * The result of the search for a creator method by {@link CreatorMethodFinderStrategy}
 */
public sealed interface CreatorMethod {
    /**
     * The found creator method
     */
    record Found(ExecutableElement constructor) implements CreatorMethod {}

    /**
     * An error occurred during the search
     */
    enum Error implements CreatorMethod {
        /**
         * The {@link CreatorMethodFinderStrategy} could not be applied to the annotated element
         */
        NOT_APPLICABLE,
        /**
         * No valid creator method was found
         */
        NOT_FOUND,
        /**
         * More than one valid creator method was found
         */
        TOO_MANY_FOUND
    }
}
