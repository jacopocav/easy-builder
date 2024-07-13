package com.github.jacopocav.builder.internal.validation;

import com.github.jacopocav.builder.processing.validation.JavaNameValidator;
import com.github.jacopocav.builder.processing.validation.rule.ElementKindValidationRule;
import com.github.jacopocav.builder.processing.validation.rule.SamePackageAccessValidationRule;
import com.github.jacopocav.builder.processing.validation.rule.ValidationRule;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static javax.lang.model.element.ElementKind.*;

public class ValidationRules {
    private ValidationRules() {}

    public static Collection<ValidationRule> getAll(JavaNameValidator javaNameValidator) {
        return List.of(
                new OptionsValidationRule(javaNameValidator),
                new SamePackageAccessValidationRule(),
                new ClassValidationRule(),
                new ConstructorValidationRule(),
                new ElementKindValidationRule(Set.of(RECORD, CLASS, CONSTRUCTOR, METHOD)),
                new MethodValidationRule());
    }
}
