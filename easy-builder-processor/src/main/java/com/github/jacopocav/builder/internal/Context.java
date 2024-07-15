package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import com.github.jacopocav.builder.processing.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.processing.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.processing.writer.GeneratedJavaFileWriter;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Map;

/**
 * Generates the main collaborators needed by {@link BuilderProcessor}
 */
public interface Context {

    SingleElementJavaFileGenerator singleBuilderJavaFileGenerator();

    OptionCompilerArgumentsValidator optionCompilerArgumentsValidator();

    ProcessingExceptionPrinter processingExceptionPrinter();

    GeneratedJavaFileWriter generatedJavaFileWriter();

    static Context createDefault(ProcessingEnvironment processingEnvironment) {
        return new ContextImpl(processingEnvironment);
    }
}
