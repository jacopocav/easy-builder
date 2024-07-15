package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.processing.error.ProcessingException;
import java.util.Collection;
import java.util.Map;

public interface OptionCompilerArgumentsValidator {
    /**
     * Validates the options received by the compiler
     * @return all the errors encountered during validation,
     * or an empty collection if the options are valid.
     */
    Collection<ProcessingException> validate(Map<String, String> options);
}
