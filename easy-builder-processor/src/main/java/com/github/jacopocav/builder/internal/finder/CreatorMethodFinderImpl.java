package com.github.jacopocav.builder.internal.finder;

import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Found;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethodFinderStrategy;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.Collection;
import java.util.List;

import static com.github.jacopocav.builder.processing.error.ProcessingException.processingException;
import static com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error.*;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

public class CreatorMethodFinderImpl implements CreatorMethodFinder {
    private final List<? extends CreatorMethodFinderStrategy> strategies;

    public CreatorMethodFinderImpl(Collection<? extends CreatorMethodFinderStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    @Override
    public ExecutableElement find(Element annotatedElement) {
        var result = strategies.stream()
                .map(strategy -> strategy.find(annotatedElement))
                .filter(not(isEqual(NOT_APPLICABLE)))
                .findFirst()
                .orElse(NOT_APPLICABLE);

        return switch (result) {
            case NOT_FOUND -> throw processingException(
                    annotatedElement,
                    "no valid constructor found. A non-private constructor with at least 1 argument must be present");
            case TOO_MANY_FOUND -> throw processingException(
                    annotatedElement,
                    "found multiple non-private constructors with at least 1 argument. Move the @Builder annotation to the constructor you want the builder to use");
            case NOT_APPLICABLE -> throw new AssertionError(
                    "no valid creator finder strategy found (this should not happen)");
            case Found f -> f.constructor();
        };
    }
}
