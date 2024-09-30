package com.github.jacopocav.builder.processor;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.internal.Context;
import com.github.jacopocav.builder.internal.error.AggregatedProcessingException;
import com.github.jacopocav.builder.internal.error.ProcessingException;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Annotation processor that generates builder classes from elements annotated with {@link Builder}
 */
public class BuilderProcessor extends AbstractProcessor {
    private Context context;
    private Map<String, String> nonNullArguments;

    private boolean argsValidated = false;

    @SuppressWarnings("unused") // necessary for SPI mechanism
    public BuilderProcessor() {}

    BuilderProcessor(Context context) {
        this.context = context;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Builder.class.getName());
    }

    @Override
    public Set<String> getSupportedOptions() {
        return BuilderOption.all().stream().map(BuilderOption::compilerName).collect(toUnmodifiableSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        initNonNullOptions(processingEnv);
        context = requireNonNullElseGet(context, () -> Context.createDefault(processingEnv));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (validateCompilerArguments()) {
            annotations.forEach(
                    annotation -> roundEnv.getElementsAnnotatedWith(annotation).forEach(this::processElement));
        }

        return true;
    }

    private void initNonNullOptions(ProcessingEnvironment processingEnv) {
        nonNullArguments = processingEnv.getOptions().entrySet().stream()
                .collect(toUnmodifiableMap(Entry::getKey, e -> requireNonNullElse(e.getValue(), "")));
    }

    /**
     * Validates compiler options only once, to avoid printing duplicate error messages
     * @return {@code true} if the options are valid, {@code false} otherwise
     */
    private boolean validateCompilerArguments() {
        if (argsValidated) {
            return true;
        }

        var errors = context.optionCompilerArgumentsValidator().validate(nonNullArguments);
        argsValidated = true;

        errors.forEach(context.processingExceptionPrinter()::print);
        return errors.isEmpty();
    }

    private void processElement(Element element) {
        var exceptionPrinter = context.processingExceptionPrinter();
        try {
            var generatedJavaFile = context.builderGenerator().generate(element);
            context.generatedJavaFileWriter().write(generatedJavaFile);
        } catch (ProcessingException e) {
            exceptionPrinter.print(e);
        } catch (AggregatedProcessingException e) {
            exceptionPrinter.print(e);
        } catch (UncheckedIOException e) {
            exceptionPrinter.print(processingException(
                    element,
                    "I/O error occurred while writing builder source: %s",
                    e.getCause().getMessage()));
        }
    }
}
