package com.github.jacopocav.builder.internal.finder;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

/**
 * The result of the search for an accessor made by {@link AccessorFinder}
 */
public sealed interface Accessor {
    /**
     * The constructor/method argument for which an accessor (getter method or field) must be found
     */
    VariableElement argument();

    /**
     * The found accessor
     */
    record Found(VariableElement argument, Element accessor) implements Accessor {}

    /**
     * The accessor could not be found
     */
    record NotFound(VariableElement argument) implements Accessor {}
}
