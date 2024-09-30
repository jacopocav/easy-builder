package com.github.jacopocav.builder.internal.validation;

/**
 * Validates whether a string is a valid Java identifier name or name prefix.
 * <p>An invalid name (or name prefix) will cause compilation to fail.
 */
public interface JavaNameValidator {
    boolean isValidName(String value);

    boolean isValidNamePrefix(String value);
}
