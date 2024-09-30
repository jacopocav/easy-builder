package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import com.github.jacopocav.builder.internal.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.internal.template.BuilderTemplateRenderer;
import com.github.jacopocav.builder.internal.validation.ElementValidator;
import javax.lang.model.element.Element;

public class BuilderGenerator {
    private final ElementValidator elementValidator;
    private final CreatorMethodFinder creatorMethodFinder;
    private final OptionsRepository optionsRepository;
    private final BuilderTemplateRenderer generator;

    BuilderGenerator(
            ElementValidator elementValidator,
            CreatorMethodFinder creatorMethodFinder,
            OptionsRepository optionsRepository,
            BuilderTemplateRenderer generator) {

        this.elementValidator = elementValidator;
        this.creatorMethodFinder = creatorMethodFinder;
        this.optionsRepository = optionsRepository;
        this.generator = generator;
    }

    /**
     * Generates a builder source file from a single annotated {@code element}.
     */
    public GeneratedJavaFile generate(Element element) {
        elementValidator.validate(element);

        var creatorMethod = creatorMethodFinder.find(element);
        var rawOptions = optionsRepository.getRaw(element);

        return generator.render(rawOptions, creatorMethod);
    }
}
