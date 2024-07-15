package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.BuilderGenerator;
import com.github.jacopocav.builder.internal.SourceClassRetriever;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.option.RawOptions;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeNameGenerator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Generates a builder class from a jte template ({@code src/main/jte/builder.jte})
 */
public class BuilderGeneratorJte implements BuilderGenerator {

    private final SourceClassRetriever sourceClassRetriever;
    private final OptionsRepository optionsRepository;
    private final GeneratedTypeNameGenerator generatedTypeNameGenerator;
    private final JteModelCreator jteModelCreator;

    public BuilderGeneratorJte(
            SourceClassRetriever sourceClassRetriever,
            OptionsRepository optionsRepository,
            GeneratedTypeNameGenerator generatedTypeNameGenerator,
            JteModelCreator jteModelCreator) {
        this.sourceClassRetriever = sourceClassRetriever;
        this.optionsRepository = optionsRepository;
        this.generatedTypeNameGenerator = generatedTypeNameGenerator;
        this.jteModelCreator = jteModelCreator;
    }

    @Override
    public GeneratedJavaFile generate(RawOptions rawOptions, ExecutableElement creatorMethod) {
        var enclosingClass = (TypeElement) creatorMethod.getEnclosingElement();
        var sourceClass = sourceClassRetriever.getElement(creatorMethod);
        var interpolatedOptions = optionsRepository.getInterpolated(rawOptions, sourceClass);
        var builderName = generatedTypeNameGenerator.generate(enclosingClass, sourceClass, interpolatedOptions.className());

        var builderData = new BuilderData(builderName, interpolatedOptions, creatorMethod, sourceClass, enclosingClass);
        var jteModel = jteModelCreator.create(builderData);

        var qualifiedName =
                builderName.enclosingPackage().getQualifiedName().toString() + "." + builderName.simpleName();
        return new GeneratedJavaFile(qualifiedName, jteModel.render());
    }
}
