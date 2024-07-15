package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import com.github.jacopocav.builder.internal.option.OptionsRepository;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.processing.validation.ElementValidator;
import javax.lang.model.element.Element;

class SingleBuilderJavaFileGenerator implements SingleElementJavaFileGenerator {
    private final ElementValidator elementValidator;
    private final CreatorMethodFinder creatorMethodFinder;
    private final OptionsRepository optionsRepository;
    private final BuilderGenerator generator;

    SingleBuilderJavaFileGenerator(
            ElementValidator elementValidator,
            CreatorMethodFinder creatorMethodFinder,
            OptionsRepository optionsRepository,
            BuilderGenerator generator) {

        this.elementValidator = elementValidator;
        this.creatorMethodFinder = creatorMethodFinder;
        this.optionsRepository = optionsRepository;
        this.generator = generator;
    }

    public GeneratedJavaFile generate(Element element) {
        elementValidator.validate(element);

        var creatorMethod = creatorMethodFinder.find(element);
        var rawOptions = optionsRepository.getRaw(element);

        return generator.generate(rawOptions, creatorMethod);
    }
}
