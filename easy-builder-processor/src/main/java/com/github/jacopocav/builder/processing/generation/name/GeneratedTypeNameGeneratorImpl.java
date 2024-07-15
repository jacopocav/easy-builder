package com.github.jacopocav.builder.processing.generation.name;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

public class GeneratedTypeNameGeneratorImpl implements GeneratedTypeNameGenerator {
    private final Elements elements;

    public GeneratedTypeNameGeneratorImpl(Elements elements) {
        this.elements = elements;
    }

    @Override
    public GeneratedTypeName generate(Element samePackageElement, String simpleNameOverride) {
        var enclosingPackage = elements.getPackageOf(samePackageElement);
        return new GeneratedTypeName(enclosingPackage, simpleNameOverride);
    }
}
