package com.github.jacopocav.builder.internal.validation;

import com.github.jacopocav.builder.internal.generation.name.NameTemplateInterpolator;
import javax.lang.model.SourceVersion;

/**
 * Validates whether a string is a valid Java identifier name or name prefix.
 * <p>An invalid name (or name prefix) will cause compilation to fail.
 */
public class JavaNameValidator {
    private final NameTemplateInterpolator nameTemplateInterpolator;

    public JavaNameValidator(NameTemplateInterpolator nameTemplateInterpolator) {
        this.nameTemplateInterpolator = nameTemplateInterpolator;
    }

    public boolean isValidName(String value) {
        return SourceVersion.isName(nameTemplateInterpolator.interpolate(value, Object.class));
    }

    public boolean isValidNamePrefix(String value) {
        return value.isEmpty() || SourceVersion.isIdentifier(nameTemplateInterpolator.interpolate(value, Object.class));
    }
}
