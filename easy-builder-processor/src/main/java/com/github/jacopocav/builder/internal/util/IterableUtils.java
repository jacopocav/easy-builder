package com.github.jacopocav.builder.internal.util;

import java.util.List;

public class IterableUtils {
    private IterableUtils() {}

    public static <T> T getFirst(List<T> list) {
        return list.get(0);
    }

    public static <T> T getFirst(Iterable<T> iterable) {
        return iterable.iterator().next();
    }
}
