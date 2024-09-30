package com.github.jacopocav.builder.internal.validation;

import static com.github.jacopocav.builder.internal.error.AggregatedProcessingException.processingExceptions;

import com.github.jacopocav.builder.internal.error.AggregatedProcessingException;
import com.github.jacopocav.builder.internal.validation.rule.ValidationRule;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;

public class ElementValidator {
    private final List<? extends ValidationRule> rules;

    public ElementValidator(Collection<? extends ValidationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Ensures that {@code element} satisfies some {@link ValidationRule}s.
     *
     * @throws AggregatedProcessingException if
     * {@code element} is not valid.
     */
    public void validate(Element element) {
        var errors = rules.stream()
                .filter(v -> v.supports(element))
                .map(v -> v.apply(element))
                .flatMap(Collection::stream)
                .toList();

        if (!errors.isEmpty()) {
            throw processingExceptions(errors);
        }
    }
}
