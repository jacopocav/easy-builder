package com.github.jacopocav.builder.internal.finder.strategy;

import java.util.Collection;
import java.util.List;

/**
 * Factory for all implementations of {@link CreatorMethodFinderStrategy}
 */
public class CreatorMethodFinderStrategies {
    private CreatorMethodFinderStrategies() {}

    public static Collection<CreatorMethodFinderStrategy> getAll() {
        return List.of(
                new ClassConstructorFinderStrategy(),
                new RecordConstructorFinderStrategy(),
                new ExecutableElementFinderStrategy());
    }
}
