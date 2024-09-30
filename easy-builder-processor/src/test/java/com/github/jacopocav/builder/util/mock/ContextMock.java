package com.github.jacopocav.builder.util.mock;

import com.github.jacopocav.builder.internal.Context;
import com.github.jacopocav.builder.internal.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.internal.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.internal.writer.GeneratedJavaFileWriter;

public record ContextMock(
        SingleElementJavaFileGenerator singleElementJavaFileGenerator,
        OptionCompilerArgumentsValidator optionCompilerArgumentsValidator,
        GeneratedJavaFileWriter generatedJavaFileWriter,
        ProcessingExceptionPrinter processingExceptionPrinter)
        implements Context {

    @Override
    public SingleElementJavaFileGenerator singleBuilderJavaFileGenerator() {
        return singleElementJavaFileGenerator;
    }
}
