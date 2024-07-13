package com.github.jacopocav.builder.processing.generation.name;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

public class GeneratedTypeNameGeneratorImpl implements GeneratedTypeNameGenerator {
    private final String defaultSuffix;
    private final Elements elements;

    public GeneratedTypeNameGeneratorImpl(String defaultSuffix, Elements elements) {
        this.defaultSuffix = defaultSuffix;
        this.elements = elements;
    }

    @Override
    public GeneratedTypeName generate(
            Element samePackageElement,
            TypeElement originatingType,
            String simpleNameOverride
    ) {
        var enclosingPackage = elements.getPackageOf(samePackageElement);
        var simpleName = requireNonNullElseGet(simpleNameOverride, () -> defaultSimpleName(originatingType));
        return new GeneratedTypeName(enclosingPackage, simpleName);
    }

    @Override
    public GeneratedTypeName generate(
            TypeElement originatingType,
            String simpleNameOverride
    ) {
        return generate(originatingType, originatingType, simpleNameOverride);
    }

    private String defaultSimpleName(TypeElement sourceClass) {
        if (sourceClass.getNestingKind() == TOP_LEVEL) {
            return sourceClass.getSimpleName().toString() + defaultSuffix;
        }

        return joinNestedSimpleNames(sourceClass);
    }

    private String joinNestedSimpleNames(TypeElement memberTypeElement) {
        return Stream.iterate(memberTypeElement, el -> el.getKind().isDeclaredType(), Element::getEnclosingElement)
                .map(Element::getSimpleName)
                .sorted(reversed())
                .collect(joining("_", "", defaultSuffix));
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private static <T> Comparator<T> reversed() {
        return (a, b) -> -1;
    }
}
