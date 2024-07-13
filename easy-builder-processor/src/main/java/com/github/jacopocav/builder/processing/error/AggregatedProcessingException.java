package com.github.jacopocav.builder.processing.error;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * A container for multiple {@link ProcessingException ProcessingExceptions}
 */
public class AggregatedProcessingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Collection<ProcessingException> errors;

    public AggregatedProcessingException(Collection<ProcessingException> errors) {
        this.errors = errors;
    }

    public Collection<ProcessingException> errors() {
        return errors;
    }

    public static AggregatedProcessingException processingExceptions(Collection<? extends ProcessingException> errors) {
        return new AggregatedProcessingException(List.copyOf(errors));
    }
}
