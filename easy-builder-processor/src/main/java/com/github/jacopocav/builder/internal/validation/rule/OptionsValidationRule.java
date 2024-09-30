package com.github.jacopocav.builder.internal.validation.rule;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static java.util.stream.Collectors.toMap;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.internal.error.ProcessingException;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.validation.JavaNameValidator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.Element;

/**
 * Ensures that the {@code element} annotated with
 * {@link Builder @Builder} has valid option values
 * (i.e. all are valid java identifier names or name prefixes).
 */
class OptionsValidationRule implements ValidationRule {
    private final JavaNameValidator javaNameValidator;

    OptionsValidationRule(JavaNameValidator javaNameValidator) {
        this.javaNameValidator = javaNameValidator;
    }

    @Override
    public boolean supports(Element element) {
        return true;
    }

    @Override
    public Collection<ProcessingException> apply(Element element) {
        var annotation = element.getAnnotationMirrors().stream()
                .filter(ann -> ann.getAnnotationType().toString().equals(Builder.class.getName()))
                .findFirst();

        if (annotation.isEmpty()) {
            return List.of(processingException(element, "element is not annotated with @Builder"));
        }

        var annotationOptions = annotation.get().getElementValues().entrySet().stream()
                .collect(toMap(
                        e -> e.getKey().getSimpleName().toString(),
                        e -> e.getValue().getValue().toString()));

        return BuilderOption.all().stream()
                .map(option -> validateOption(annotationOptions, option, element))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ProcessingException> validateOption(
            Map<String, String> annotationOptions, BuilderOption option, Element element) {

        if (!annotationOptions.containsKey(option.annotationName())) {
            return Optional.empty();
        }

        var value = annotationOptions.get(option.annotationName());

        if (isValidOptionValue(option, value)) {
            return Optional.empty();
        }

        return Optional.of(processingException(
                element,
                "value \"%s\" of attribute %s is not a permitted Java identifier",
                annotationOptions.get(option.annotationName()),
                option.annotationName()));
    }

    private boolean isValidOptionValue(BuilderOption option, String value) {
        return switch (option) {
            case SETTER_PREFIX -> javaNameValidator.isValidNamePrefix(value);
            case CLASS_NAME -> value.isEmpty() || javaNameValidator.isValidName(value);
            case BUILD_METHOD_NAME, FACTORY_METHOD_NAME, COPY_FACTORY_METHOD_NAME -> javaNameValidator.isValidName(
                    value);
            case COPY_FACTORY_METHOD -> true;
        };
    }
}
