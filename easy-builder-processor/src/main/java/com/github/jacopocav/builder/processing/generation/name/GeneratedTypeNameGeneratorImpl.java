package com.github.jacopocav.builder.processing.generation.name;

import com.github.jacopocav.builder.annotation.Builder;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Comparator;
import java.util.stream.Stream;

import static com.github.jacopocav.builder.internal.util.MultiReleaseUtils.isDeclaredType;
import static com.github.jacopocav.builder.internal.util.StringUtils.decapitalize;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

public class GeneratedTypeNameGeneratorImpl implements GeneratedTypeNameGenerator {
    private final String defaultTemplate;
    private final Elements elements;

    public GeneratedTypeNameGeneratorImpl(String defaultTemplate, Elements elements) {
        this.defaultTemplate = defaultTemplate;
        this.elements = elements;
    }

    @Override
    public GeneratedTypeName generate(
            Element samePackageElement, TypeElement originatingType, String simpleNameOverride) {
        var enclosingPackage = elements.getPackageOf(samePackageElement);
        var fullSimpleName = fullSimpleNameOf(originatingType);
        var simpleName = requireNonNullElse(simpleNameOverride, defaultTemplate)
                .replace(Builder.SOURCE_CLASS_NAME, fullSimpleName)
                .replace(Builder.LOWER_CASE_SOURCE_CLASS_NAME, decapitalize(fullSimpleName));
        return new GeneratedTypeName(enclosingPackage, simpleName);
    }

    @Override
    public GeneratedTypeName generate(TypeElement originatingType, String simpleNameOverride) {
        return generate(originatingType, originatingType, simpleNameOverride);
    }

    private String fullSimpleNameOf(TypeElement sourceClass) {
        if (sourceClass.getNestingKind() == TOP_LEVEL) {
            return sourceClass.getSimpleName().toString();
        }

        return joinNestedSimpleNames(sourceClass);
    }

    private String joinNestedSimpleNames(TypeElement memberTypeElement) {
        return Stream.iterate(memberTypeElement, el -> isDeclaredType(el.getKind()), Element::getEnclosingElement)
                .map(Element::getSimpleName)
                .sorted(reversed())
                .collect(joining("_"));
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private static <T> Comparator<T> reversed() {
        return (a, b) -> -1;
    }
}
