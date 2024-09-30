package com.github.jacopocav.builder.internal.validation.rule;

import static javax.lang.model.element.ElementKind.*;

import com.github.jacopocav.builder.internal.validation.JavaNameValidator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
