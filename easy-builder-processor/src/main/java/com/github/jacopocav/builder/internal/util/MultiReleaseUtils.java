package com.github.jacopocav.builder.internal.util;

import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

public class MultiReleaseUtils {
    private MultiReleaseUtils() {}

    public static boolean isDeclaredType(ElementKind elementKind) {
        return elementKind.isClass() || elementKind.isInterface();
    }

    public static <T> T getFirst(List<T> list) {
        return list.get(0);
    }

    public static <T> T getFirst(Set<T> set) {
        return set.iterator().next();
    }
}
