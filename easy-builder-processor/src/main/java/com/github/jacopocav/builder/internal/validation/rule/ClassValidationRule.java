package com.github.jacopocav.builder.internal.validation.rule;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;

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
        var enclosingKind = element.getEnclosingElement().getKind();
        return (enclosingKind.isClass() || enclosingKind.isInterface())
                && !element.getModifiers().contains(STATIC);
    }
}
