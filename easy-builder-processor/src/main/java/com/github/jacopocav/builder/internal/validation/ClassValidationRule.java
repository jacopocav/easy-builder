package com.github.jacopocav.builder.internal.validation;

import com.github.jacopocav.builder.processing.error.ProcessingException;
import com.github.jacopocav.builder.processing.validation.rule.ValidationRule;

import javax.lang.model.element.Element;
import java.util.Collection;
import java.util.List;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Ensures that the class annotated with
 * {@link com.github.jacopocav.builder.annotation.Builder @Builder} is valid
 * (i.e. is either a top-level class or a static inner class).
 */
final class ClassValidationRule implements ValidationRule {

    @Override
    public boolean supports(Element element) {
        return element.getKind() == CLASS;
    }

    @Override
    public Collection<ProcessingException> apply(Element element) {
        if (isNonStaticInnerClass(element)) {
            return List.of(ProcessingException.processingException(element, "annotated class must be static"));
        }

        return List.of();
    }

    private boolean isNonStaticInnerClass(Element element) {
        return element.getEnclosingElement().getKind().isDeclaredType()
                && !element.getModifiers().contains(STATIC);
    }
}
