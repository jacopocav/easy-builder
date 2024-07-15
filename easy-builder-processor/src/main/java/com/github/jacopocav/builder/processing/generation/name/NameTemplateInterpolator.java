package com.github.jacopocav.builder.processing.generation.name;

import com.github.jacopocav.builder.annotation.Builder;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.jacopocav.builder.internal.util.MultiReleaseUtils.isDeclaredType;
import static com.github.jacopocav.builder.internal.util.StringUtils.decapitalize;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

public class NameTemplateInterpolator {
    public String interpolate(String template, Class<?> sourceClass) {
        return interpolate(template, fullSimpleNameOf(sourceClass));
    }

    public String interpolate(String template, TypeElement sourceType) {
        return interpolate(template, fullSimpleNameOf(sourceType));
    }

    private String interpolate(String template, String fullSimpleName) {
        var result = template;

        if (template.contains(Builder.SOURCE_CLASS_NAME)) {
            result = result.replace(Builder.SOURCE_CLASS_NAME, fullSimpleName);
        }
        if (template.contains(Builder.LOWER_CASE_SOURCE_CLASS_NAME)) {
            result = result.replace(Builder.LOWER_CASE_SOURCE_CLASS_NAME, decapitalize(fullSimpleName));
        }

        return result;
    }

    private String fullSimpleNameOf(Class<?> sourceClass) {
        if (sourceClass.getEnclosingClass() == null) {
            return sourceClass.getSimpleName();
        }

        return joinNestedSimpleNames(sourceClass);
    }

    @SuppressWarnings("rawtypes")
    private String joinNestedSimpleNames(Class<?> memberTypeElement) {
        return Stream.<Class>iterate(memberTypeElement, Objects::nonNull, Class::getEnclosingClass)
                .map(Class::getSimpleName)
                .sorted(reversed())
                .collect(joining("_"));
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
