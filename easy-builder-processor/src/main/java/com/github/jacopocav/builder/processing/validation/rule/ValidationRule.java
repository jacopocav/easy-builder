package com.github.jacopocav.builder.processing.validation.rule;

import com.github.jacopocav.builder.processing.validation.ElementValidator;
import com.github.jacopocav.builder.processing.error.ProcessingException;

import javax.lang.model.element.Element;
import java.util.Collection;

/**
 * A validation rule used by {@link  ElementValidator}
 */
public interface ValidationRule {

    /**
     * @return {@code true} if this rule can be applied to the given {@code element}, {@code false} otherwise
     */
    boolean supports(Element element);

    /**
     * Validates this rule against the given {@code element}
     * @return all the errors encountered by the rule, or an empty collection
     * if {@code element} satisfies the rule
     */
    Collection<ProcessingException> apply(Element element);
}