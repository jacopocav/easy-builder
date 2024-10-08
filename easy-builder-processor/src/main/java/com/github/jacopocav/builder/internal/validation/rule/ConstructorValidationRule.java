package com.github.jacopocav.builder.internal.validation.rule;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.ABSTRACT;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Ensures that the constructor annotated with
 * {@link com.github.jacopocav.builder.annotation.Builder @Builder} is valid
 * (i.e. it's declared in a concrete class and must have at least one argument).
 */
final class ConstructorValidationRule implements ValidationRule {

    @Override
    public boolean supports(Element element) {
        return element.getKind() == CONSTRUCTOR;
    }

    @Override
    public List<ProcessingException> apply(Element element) {
        var errors = new ArrayList<ProcessingException>();

        if (isInsideAbstractClass(element)) {
            errors.add(processingException(element, "class of annotated constructor must not be abstract"));
        }
        if (hasZeroArgs(element)) {
            errors.add(processingException(element, "annotated constructor must have at least one argument"));
        }

        return List.copyOf(errors);
    }

    private boolean hasZeroArgs(Element element) {
        return ((ExecutableElement) element).getParameters().isEmpty();
    }

    private boolean isInsideAbstractClass(Element element) {
        return element.getEnclosingElement().getModifiers().contains(ABSTRACT);
    }
}
