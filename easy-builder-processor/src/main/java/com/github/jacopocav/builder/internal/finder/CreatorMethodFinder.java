package com.github.jacopocav.builder.internal.finder;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error.NOT_APPLICABLE;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Found;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethodFinderStrategy;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public class CreatorMethodFinder {
    private final List<? extends CreatorMethodFinderStrategy> strategies;

    public CreatorMethodFinder(Collection<? extends CreatorMethodFinderStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Finds the <em>creator method</em> (i.e. the constructor or static factory method that will
     * be used by the builder to build new instances of the target type).
     *
     * @param annotatedElement the element annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder}
     * @return the creator method
     */
    public ExecutableElement find(Element annotatedElement) {
        var result = strategies.stream()
                .map(strategy -> strategy.find(annotatedElement))
                .filter(not(isEqual(NOT_APPLICABLE)))
                .findFirst()
                .orElse(NOT_APPLICABLE);

        if (result instanceof Error err) {
            switch (err) {
                case NOT_FOUND -> throw processingException(
                        annotatedElement,
                        "no valid constructor found. A non-private constructor with at least 1 argument must be present");
                case TOO_MANY_FOUND -> throw processingException(
                        annotatedElement,
                        "found multiple non-private constructors with at least 1 argument. Move the @Builder annotation to the constructor you want the builder to use");
                case NOT_APPLICABLE -> throw new AssertionError(
                        "no valid creator finder strategy found (this should not happen)");
            }
        }

        return ((Found) result).constructor();
    }
}
