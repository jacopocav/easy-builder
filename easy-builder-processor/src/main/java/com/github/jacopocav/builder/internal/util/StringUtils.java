package com.github.jacopocav.builder.internal.util;

import static java.lang.Character.toTitleCase;

public class StringUtils {
    private StringUtils() {}

    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    /**
     * Returns the input {@code string} with the first character converted to upper-case
     */
    public static String capitalize(String string) {
        if (isNullOrBlank(string)) {
            return string;
        }

        var builder = new StringBuilder(string);
        builder.setCharAt(0, toTitleCase(builder.charAt(0)));
        return builder.toString();
    }

    public static String composeSetterName(String prefix, String argumentName) {
        return isNullOrBlank(prefix) ? argumentName : prefix + capitalize(argumentName);
    }
}
