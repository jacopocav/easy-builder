package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.annotation.Builder.Defaults;
import com.github.jacopocav.builder.internal.finder.AccessorFinder;
import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import com.github.jacopocav.builder.internal.finder.CreatorMethodFinderImpl;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethodFinderStrategies;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidatorImpl;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.option.OptionsRepositoryImpl;
import com.github.jacopocav.builder.internal.template.JteModelCreator;
import com.github.jacopocav.builder.processing.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.processing.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeNameGeneratorImpl;
import com.github.jacopocav.builder.processing.type.TypeRegistryFactory;
import com.github.jacopocav.builder.processing.validation.ElementValidator;
import com.github.jacopocav.builder.processing.validation.ElementValidatorImpl;
import com.github.jacopocav.builder.processing.validation.JavaNameValidatorImpl;
import com.github.jacopocav.builder.processing.writer.GeneratedJavaFileWriter;
import com.github.jacopocav.builder.internal.template.BuilderGeneratorJte;
import com.github.jacopocav.builder.internal.template.MembersGenerator;
import com.github.jacopocav.builder.internal.template.MetadataAnnotationsGenerator;
import com.github.jacopocav.builder.internal.template.jte.StaticTemplates;
import com.github.jacopocav.builder.internal.validation.ValidationRules;

import javax.annotation.processing.ProcessingEnvironment;
import java.time.Clock;
import java.util.Map;

class ContextImpl implements Context {
    private final ElementValidator elementValidator;
    private final CreatorMethodFinder creatorMethodFinder;
    private final OptionCompilerArgumentsValidator optionCompilerArgumentsValidator;
    private final ProcessingExceptionPrinter processingExceptionPrinter;
    private final BuilderGenerator builderGenerator;
    private final GeneratedJavaFileWriter generatedJavaFileWriter;

    ContextImpl(ProcessingEnvironment processingEnvironment) {
        var javaNameValidator = new JavaNameValidatorImpl();
        var types = processingEnvironment.getTypeUtils();
        var elements = processingEnvironment.getElementUtils();
        var sourceClassRetriever = new SourceClassRetriever(types);

        creatorMethodFinder = new CreatorMethodFinderImpl(CreatorMethodFinderStrategies.getAll());
        elementValidator = new ElementValidatorImpl(ValidationRules.getAll(javaNameValidator));
        builderGenerator = new BuilderGeneratorJte(
                new SourceClassRetriever(types),
                new GeneratedTypeNameGeneratorImpl(Defaults.CLASS_NAME, elements),
                new JteModelCreator(
                        Clock.systemDefaultZone(),
                        new MembersGenerator(new AccessorFinder(types, elements, sourceClassRetriever)),
                        new MetadataAnnotationsGenerator(),
                        new TypeRegistryFactory(),
                        new StaticTemplates()));
        optionCompilerArgumentsValidator = new OptionCompilerArgumentsValidatorImpl(javaNameValidator);
        processingExceptionPrinter = new ProcessingExceptionPrinter(
                "@Builder processing error: ", processingEnvironment.getMessager(), false);
        generatedJavaFileWriter = new GeneratedJavaFileWriter(processingEnvironment.getFiler());
    }

    @Override
    public SingleElementJavaFileGenerator singleBuilderJavaFileGenerator(Map<String, String> options) {
        return new SingleBuilderJavaFileGenerator(
                elementValidator, creatorMethodFinder, builderGenerator, defaultOptionsRepository(options));
    }

    @Override
    public OptionCompilerArgumentsValidator optionCompilerArgumentsValidator() {
        return optionCompilerArgumentsValidator;
    }

    private OptionsRepository defaultOptionsRepository(Map<String, String> options) {
        return new OptionsRepositoryImpl(options);
    }

    @Override
    public GeneratedJavaFileWriter generatedJavaFileWriter() {
        return generatedJavaFileWriter;
    }

    @Override
    public ProcessingExceptionPrinter processingExceptionPrinter() {
        return processingExceptionPrinter;
    }
}
