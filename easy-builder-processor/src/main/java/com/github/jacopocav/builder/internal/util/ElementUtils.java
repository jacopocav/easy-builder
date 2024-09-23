package com.github.jacopocav.builder.internal.util;

import javax.lang.model.element.ElementKind;

public class ElementUtils {
    private ElementUtils() {}

    public static boolean isDeclaredType(ElementKind elementKind) {
        return elementKind.isClass() || elementKind.isInterface();
    }
}
