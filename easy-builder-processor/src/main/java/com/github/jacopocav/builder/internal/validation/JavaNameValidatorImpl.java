package com.github.jacopocav.builder.internal.validation;

import com.github.jacopocav.builder.internal.generation.name.NameTemplateInterpolator;
import javax.lang.model.SourceVersion;

public class JavaNameValidatorImpl implements JavaNameValidator {
    private final NameTemplateInterpolator nameTemplateInterpolator;

    public JavaNameValidatorImpl(NameTemplateInterpolator nameTemplateInterpolator) {
        this.nameTemplateInterpolator = nameTemplateInterpolator;
    }

    @Override
    public boolean isValidName(String value) {
        return SourceVersion.isName(nameTemplateInterpolator.interpolate(value, Object.class));
    }

    @Override
    public boolean isValidNamePrefix(String value) {
        return value.isEmpty() || SourceVersion.isIdentifier(nameTemplateInterpolator.interpolate(value, Object.class));
    }
}
