package com.github.jacopocav.builder.internal;

import com.github.jacopocav.builder.internal.finder.CreatorMethodFinder;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.processing.validation.ElementValidator;
import com.github.jacopocav.builder.internal.option.OptionsRepository;

import javax.lang.model.element.Element;

class SingleBuilderJavaFileGenerator implements SingleElementJavaFileGenerator {
    private final ElementValidator elementValidator;
    private final CreatorMethodFinder creatorMethodFinder;
    private final BuilderGenerator generator;
    private final OptionsRepository optionsRepository;

    SingleBuilderJavaFileGenerator(
            ElementValidator elementValidator,
            CreatorMethodFinder creatorMethodFinder,
            BuilderGenerator generator,
            OptionsRepository optionsRepository) {

        this.elementValidator = elementValidator;
        this.creatorMethodFinder = creatorMethodFinder;
        this.generator = generator;
        this.optionsRepository = optionsRepository;
    }

    public GeneratedJavaFile generate(Element element) {
        elementValidator.validate(element);

        var creatorMethod = creatorMethodFinder.find(element);

        return generator.generate(creatorMethod, optionsRepository.get(element));
    }
}
