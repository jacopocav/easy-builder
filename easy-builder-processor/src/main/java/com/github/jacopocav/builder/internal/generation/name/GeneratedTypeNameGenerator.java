package com.github.jacopocav.builder.internal.generation.name;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

public class GeneratedTypeNameGenerator {
    private final Elements elements;

    public GeneratedTypeNameGenerator(Elements elements) {
        this.elements = elements;
    }

    public GeneratedTypeName generate(Element samePackageElement, String simpleNameOverride) {
        var enclosingPackage = elements.getPackageOf(samePackageElement);
        return new GeneratedTypeName(enclosingPackage, simpleNameOverride);
    }
}
