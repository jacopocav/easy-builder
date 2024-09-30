package com.github.jacopocav.builder.internal.validation.rule;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Ensures that the factory method annotated with
 * {@link com.github.jacopocav.builder.annotation.Builder @Builder} is valid
 * (i.e. it's static, has at least one argument, must not return void).
 */
final class MethodValidationRule implements ValidationRule {

    @Override
    public boolean supports(Element element) {
        return element.getKind() == METHOD;
    }

    @Override
    public List<ProcessingException> apply(Element element) {
        var errors = new ArrayList<ProcessingException>();

        if (isInstanceMethod(element)) {
            errors.add(processingException(element, "annotated method must be static"));
        }

        if (hasZeroArgs(element)) {
            errors.add(processingException(element, "annotated method must have at least one argument"));
        }

        if (returnsVoid(element)) {
            errors.add(processingException(element, "annotated method must not return void"));
        }

        return List.copyOf(errors);
    }

    private boolean isInstanceMethod(Element element) {
        return !element.getModifiers().contains(STATIC);
    }

    private boolean hasZeroArgs(Element element) {
        return ((ExecutableElement) element).getParameters().isEmpty();
    }

    private boolean returnsVoid(Element element) {
        var returnType = ((ExecutableElement) element).getReturnType();
        return returnType.getKind() == VOID || returnType.toString().equals(Void.class.getName());
    }
}
