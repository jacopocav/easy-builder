package com.github.jacopocav.builder.internal.validation.rule;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.PRIVATE;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.lang.model.element.Element;

/**
 * Ensures that {@code element} is accessible anywhere within the same package
 */
public class SamePackageAccessValidationRule implements ValidationRule {

    @Override
    public boolean supports(Element element) {
        return true;
    }

    @Override
    public Collection<ProcessingException> apply(Element element) {
        var errors = new ArrayList<ProcessingException>();

        if (isPrivate(element)) {
            errors.add(processingException(element, "annotated element must not be private"));
        }

        if (isEnclosedInPrivateClasses(element)) {
            errors.add(processingException(element, "annotated element must not be enclosed in private class"));
        }

        return List.copyOf(errors);
    }

    private boolean isPrivate(Element element) {
        return element.getModifiers().contains(PRIVATE);
    }

    private boolean isEnclosedInPrivateClasses(Element element) {
        return Stream.iterate(
                        element.getEnclosingElement(), el -> el.getKind() != PACKAGE, Element::getEnclosingElement)
                .anyMatch(this::isPrivate);
    }
}
