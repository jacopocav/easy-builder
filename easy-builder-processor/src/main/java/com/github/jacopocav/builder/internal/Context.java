package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.internal.writer.GeneratedJavaFileWriter;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Generates the main collaborators needed by {@link BuilderProcessor}
 */
public interface Context {

    BuilderGenerator builderGenerator();

    OptionCompilerArgumentsValidator optionCompilerArgumentsValidator();

    ProcessingExceptionPrinter processingExceptionPrinter();

    GeneratedJavaFileWriter generatedJavaFileWriter();

    static Context createDefault(ProcessingEnvironment processingEnvironment) {
        return new ContextImpl(processingEnvironment);
    }
}
