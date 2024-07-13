package com.github.jacopocav.builder.processing.validation;

import com.github.jacopocav.builder.processing.error.AggregatedProcessingException;
import com.github.jacopocav.builder.processing.validation.rule.ValidationRule;

import javax.lang.model.element.Element;
import java.util.Collection;
import java.util.List;

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
            throw AggregatedProcessingException.processingExceptions(errors);
        }
    }
}
