package com.github.jacopocav.builder.processing.error.printer;

import com.github.jacopocav.builder.processing.error.AggregatedProcessingException;
import com.github.jacopocav.builder.processing.error.ProcessingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

/**
 * Prints {@link ProcessingException} and {@link AggregatedProcessingException} as errors
 * using {@link Messager}
 */
public class ProcessingExceptionPrinter {
    private final String messagePrefix;
    private final Messager messager;
    private final boolean printStackTrace;

    public ProcessingExceptionPrinter(String messagePrefix, Messager messager, boolean printStackTrace) {
        this.messagePrefix = messagePrefix;
        this.messager = messager;
        this.printStackTrace = printStackTrace;
    }

    public void print(ProcessingException processingException) {
        var message = printStackTrace ? getStackTrace(processingException) : processingException.getMessage();
        messager.printMessage(Kind.ERROR, messagePrefix + message, processingException.element());
    }

    public void print(AggregatedProcessingException aggregatedProcessingException) {
        aggregatedProcessingException.errors().forEach(this::print);
    }

    private String getStackTrace(Throwable t) {
        var stringWriter = new StringWriter();

        try (var printWriter = new PrintWriter(stringWriter)) {
            t.printStackTrace(printWriter);
            printWriter.flush();
        }

        return stringWriter.toString();
    }
}
