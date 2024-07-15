package com.github.jacopocav.builder.processing.error;

import java.io.Serial;
import javax.lang.model.element.Element;

/**
 * An error that occurred during annotation processing
 */
public class ProcessingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Element element;

    private ProcessingException(Element element, String message) {
        super(message);
        this.element = element;
    }

    private ProcessingException(Element element, Throwable cause, String message) {
        super(message, cause);
        this.element = element;
    }

    /**
     * @return the element that originated the error
     */
    public Element element() {
        return element;
    }

    public static ProcessingException processingException(Element element, String message) {
        return new ProcessingException(element, message);
    }

    public static ProcessingException processingException(Element element, String message, Object... args) {
        return processingException(element, message.formatted(args));
    }

    public static ProcessingException processingException(
            Element element, Throwable cause, String message, Object... args) {
        return new ProcessingException(element, cause, message.formatted(args));
    }
}
