package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.BuilderGenerator;
import com.github.jacopocav.builder.internal.SourceClassRetriever;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeNameGenerator;
import com.github.jacopocav.builder.internal.option.Options;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Generates a builder class from a jte template ({@code src/main/jte/builder.jte})
 */
public class BuilderGeneratorJte implements BuilderGenerator {

    private final SourceClassRetriever sourceClassRetriever;
    private final GeneratedTypeNameGenerator generatedTypeNameGenerator;
    private final JteModelCreator jteModelCreator;

    public BuilderGeneratorJte(
            SourceClassRetriever sourceClassRetriever,
            GeneratedTypeNameGenerator generatedTypeNameGenerator,
            JteModelCreator jteModelCreator) {
        this.sourceClassRetriever = sourceClassRetriever;
        this.generatedTypeNameGenerator = generatedTypeNameGenerator;
        this.jteModelCreator = jteModelCreator;
    }

    @Override
    public GeneratedJavaFile generate(ExecutableElement creatorMethod, Options options) {
        var enclosingClass = (TypeElement) creatorMethod.getEnclosingElement();
        var sourceClass = sourceClassRetriever.getElement(creatorMethod);
        var builderName = generatedTypeNameGenerator.generate(
                enclosingClass, sourceClass, options.className().orElse(null));

        var builderData = new BuilderData(builderName, options, creatorMethod, sourceClass, enclosingClass);
        var jteModel = jteModelCreator.create(builderData);

        var qualifiedName =
                builderName.enclosingPackage().getQualifiedName().toString() + "." + builderName.simpleName();
        return new GeneratedJavaFile(qualifiedName, jteModel.render());
    }
}
