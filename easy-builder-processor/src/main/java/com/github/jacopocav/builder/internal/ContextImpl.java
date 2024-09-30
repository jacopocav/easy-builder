package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.internal.finder.AccessorFinder;
import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethodFinderStrategies;
import com.github.jacopocav.builder.internal.generation.name.GeneratedTypeNameGenerator;
import com.github.jacopocav.builder.internal.generation.name.NameTemplateInterpolator;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.template.BuilderTemplateRenderer;
import com.github.jacopocav.builder.internal.template.JteModelCreator;
import com.github.jacopocav.builder.internal.template.MembersGenerator;
import com.github.jacopocav.builder.internal.template.MetadataAnnotationsGenerator;
import com.github.jacopocav.builder.internal.template.jte.StaticTemplates;
import com.github.jacopocav.builder.internal.type.TypeRegistry;
import com.github.jacopocav.builder.internal.validation.ElementValidator;
import com.github.jacopocav.builder.internal.validation.JavaNameValidator;
import com.github.jacopocav.builder.internal.validation.rule.ValidationRules;
import com.github.jacopocav.builder.internal.writer.GeneratedJavaFileWriter;
import java.time.Clock;
import javax.annotation.processing.ProcessingEnvironment;

class ContextImpl implements Context {
    private final ElementValidator elementValidator;
    private final CreatorMethodFinder creatorMethodFinder;
    private final OptionCompilerArgumentsValidator optionCompilerArgumentsValidator;
    private final ProcessingExceptionPrinter processingExceptionPrinter;
    private final BuilderTemplateRenderer builderTemplateRenderer;
    private final GeneratedJavaFileWriter generatedJavaFileWriter;
    private final OptionsRepository optionsRepository;
    private final BuilderGenerator builderGenerator;

    ContextImpl(ProcessingEnvironment processingEnvironment) {
        var nameTemplateInterpolator = new NameTemplateInterpolator();
        var javaNameValidator = new JavaNameValidator(nameTemplateInterpolator);
        var types = processingEnvironment.getTypeUtils();
        var elements = processingEnvironment.getElementUtils();
        var targetClassRetriever = new SourceClassRetriever(types);

        creatorMethodFinder = new CreatorMethodFinder(CreatorMethodFinderStrategies.getAll());
        elementValidator = new ElementValidator(ValidationRules.getAll(javaNameValidator));
        optionsRepository = new OptionsRepository(processingEnvironment.getOptions(), nameTemplateInterpolator);
        builderTemplateRenderer = new BuilderTemplateRenderer(
                new SourceClassRetriever(types),
                optionsRepository,
                new GeneratedTypeNameGenerator(elements),
                new JteModelCreator(
                        Clock.systemDefaultZone(),
                        new MembersGenerator(new AccessorFinder(types, elements, targetClassRetriever)),
                        new MetadataAnnotationsGenerator(),
                        TypeRegistry::new,
                        new StaticTemplates()));
        optionCompilerArgumentsValidator = new OptionCompilerArgumentsValidator(javaNameValidator);
        processingExceptionPrinter = new ProcessingExceptionPrinter(
                "@Builder processing error: ", processingEnvironment.getMessager(), false);
        generatedJavaFileWriter = new GeneratedJavaFileWriter(processingEnvironment.getFiler());
        builderGenerator =
                new BuilderGenerator(elementValidator, creatorMethodFinder, optionsRepository, builderTemplateRenderer);
    }

    @Override
    public BuilderGenerator builderGenerator() {
        return builderGenerator;
    }

    @Override
    public OptionCompilerArgumentsValidator optionCompilerArgumentsValidator() {
        return optionCompilerArgumentsValidator;
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
