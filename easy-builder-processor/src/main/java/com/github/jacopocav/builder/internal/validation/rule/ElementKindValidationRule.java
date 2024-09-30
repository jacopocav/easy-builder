package com.github.jacopocav.builder.internal.validation.rule;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * Ensures that the annotated element is of the expected kind(s)
 */
public class ElementKindValidationRule implements ValidationRule {
    private final Set<ElementKind> validElementKinds;

    public ElementKindValidationRule(Set<ElementKind> validElementKinds) {
        this.validElementKinds = Set.copyOf(validElementKinds);
    }

    @Override
    public boolean supports(Element element) {
        return true;
    }

    @Override
    public List<ProcessingException> apply(Element element) {
        if (!validElementKinds.contains(element.getKind())) {
            return List.of(processingException(element, "unsupported annotated element kind: %s", element.getKind()));
        }

        return List.of();
    }
}
