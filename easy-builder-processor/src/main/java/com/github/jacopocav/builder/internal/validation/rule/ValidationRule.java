package com.github.jacopocav.builder.internal.validation.rule;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import com.github.jacopocav.builder.internal.validation.ElementValidator;
import java.util.Collection;
import javax.lang.model.element.Element;

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
