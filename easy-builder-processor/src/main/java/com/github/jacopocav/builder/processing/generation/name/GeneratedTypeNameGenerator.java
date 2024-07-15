package com.github.jacopocav.builder.processing.generation.name;

import javax.lang.model.element.Element;

public interface GeneratedTypeNameGenerator {
    GeneratedTypeName generate(Element samePackageElement, String simpleNameOverride);

}
