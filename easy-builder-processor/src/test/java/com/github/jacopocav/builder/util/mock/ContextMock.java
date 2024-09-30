package com.github.jacopocav.builder.util.mock;

import com.github.jacopocav.builder.internal.BuilderGenerator;
import com.github.jacopocav.builder.internal.Context;
import com.github.jacopocav.builder.internal.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.internal.writer.GeneratedJavaFileWriter;

public record ContextMock(
        BuilderGenerator builderGenerator,
        OptionCompilerArgumentsValidator optionCompilerArgumentsValidator,
        GeneratedJavaFileWriter generatedJavaFileWriter,
        ProcessingExceptionPrinter processingExceptionPrinter)
        implements Context {}
