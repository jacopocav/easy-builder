package com.github.jacopocav.builder.internal.validation;

import static com.github.jacopocav.builder.internal.error.AggregatedProcessingException.processingExceptions;

import com.github.jacopocav.builder.internal.validation.rule.ValidationRule;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;

public class ElementValidatorImpl implements ElementValidator {
    private final List<? extends ValidationRule> rules;

    public ElementValidatorImpl(Collection<? extends ValidationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
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
