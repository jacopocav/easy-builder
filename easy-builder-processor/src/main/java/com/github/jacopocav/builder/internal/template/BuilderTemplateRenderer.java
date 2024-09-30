package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.SourceClassRetriever;
import com.github.jacopocav.builder.internal.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.internal.generation.name.GeneratedTypeNameGenerator;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.option.RawOptions;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Renders a builder source file from a jte template ({@code src/main/jte/builder.jte})
 */
public class BuilderTemplateRenderer {

    private final SourceClassRetriever sourceClassRetriever;
    private final OptionsRepository optionsRepository;
    private final GeneratedTypeNameGenerator generatedTypeNameGenerator;
    private final JteModelCreator jteModelCreator;

    public BuilderTemplateRenderer(
            SourceClassRetriever sourceClassRetriever,
            OptionsRepository optionsRepository,
            GeneratedTypeNameGenerator generatedTypeNameGenerator,
            JteModelCreator jteModelCreator) {
        this.sourceClassRetriever = sourceClassRetriever;
        this.optionsRepository = optionsRepository;
        this.generatedTypeNameGenerator = generatedTypeNameGenerator;
        this.jteModelCreator = jteModelCreator;
    }

    /**
     * Generates a single builder class
     *
     * @param creatorMethod constructor or static factory method used to build target type instances
     * @return the declaration of a new builder class
     */
    public GeneratedJavaFile render(RawOptions rawOptions, ExecutableElement creatorMethod) {
        var enclosingClass = (TypeElement) creatorMethod.getEnclosingElement();
        var targetClass = sourceClassRetriever.getElement(creatorMethod);
        var interpolatedOptions = optionsRepository.getInterpolated(rawOptions, targetClass);
        var builderName = generatedTypeNameGenerator.generate(enclosingClass, interpolatedOptions.className());

        var builderData = new BuilderData(builderName, interpolatedOptions, creatorMethod, targetClass, enclosingClass);
        var jteModel = jteModelCreator.create(builderData);

        return new GeneratedJavaFile(builderName.qualifiedName(), jteModel.render());
    }
}
