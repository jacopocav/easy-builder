package com.github.jacopocav.builder.internal.finder.strategy;

import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import javax.lang.model.element.Element;

/**
 * A strategy used by {@link CreatorMethodFinder}
 * to find the creator method.
 */
@FunctionalInterface
public interface CreatorMethodFinderStrategy {
    CreatorMethod find(Element element);
}
