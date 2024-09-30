package com.github.jacopocav.builder.internal.validation;

import com.github.jacopocav.builder.internal.error.AggregatedProcessingException;
import com.github.jacopocav.builder.internal.validation.rule.ValidationRule;
import javax.lang.model.element.Element;

public interface ElementValidator {
    /**
     * Ensures that {@code element} satisfies some {@link ValidationRule}s.
     *
     * @throws AggregatedProcessingException if
     * {@code element} is not valid.
     */
    void validate(Element element);
}
