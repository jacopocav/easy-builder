package com.github.jacopocav.builder.processing.validation;

import javax.lang.model.SourceVersion;

public class JavaNameValidatorImpl implements JavaNameValidator {
    @Override
    public boolean isValidName(String value) {
        return SourceVersion.isName(value);
    }

    @Override
    public boolean isValidNamePrefix(String value) {
        return value.isEmpty() || SourceVersion.isIdentifier(value);
    }
}
