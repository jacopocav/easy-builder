package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.BuilderGenerator;
import com.github.jacopocav.builder.internal.TargetClassRetriever;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.option.RawOptions;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeNameGenerator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Generates a builder class from a jte template ({@code src/main/jte/builder.jte})
 */
public class BuilderGeneratorJte implements BuilderGenerator {

    private final TargetClassRetriever targetClassRetriever;
    private final OptionsRepository optionsRepository;
    private final GeneratedTypeNameGenerator generatedTypeNameGenerator;
    private final JteModelCreator jteModelCreator;

    public BuilderGeneratorJte(
            TargetClassRetriever targetClassRetriever,
            OptionsRepository optionsRepository,
            GeneratedTypeNameGenerator generatedTypeNameGenerator,
            JteModelCreator jteModelCreator) {
        this.targetClassRetriever = targetClassRetriever;
        this.optionsRepository = optionsRepository;
        this.generatedTypeNameGenerator = generatedTypeNameGenerator;
        this.jteModelCreator = jteModelCreator;
    }

    @Override
    public GeneratedJavaFile generate(RawOptions rawOptions, ExecutableElement creatorMethod) {
        var enclosingClass = (TypeElement) creatorMethod.getEnclosingElement();
        var targetClass = targetClassRetriever.getElement(creatorMethod);
        var interpolatedOptions = optionsRepository.getInterpolated(rawOptions, targetClass);
        var builderName = generatedTypeNameGenerator.generate(enclosingClass, interpolatedOptions.className());

        var builderData = new BuilderData(builderName, interpolatedOptions, creatorMethod, targetClass, enclosingClass);
        var jteModel = jteModelCreator.create(builderData);

        return new GeneratedJavaFile(builderName.qualifiedName(), jteModel.render());
    }
}
