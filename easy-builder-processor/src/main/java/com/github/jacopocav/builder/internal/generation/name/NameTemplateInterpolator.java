package com.github.jacopocav.builder.internal.generation.name;

import static com.github.jacopocav.builder.internal.util.ElementUtils.isDeclaredType;
import static com.github.jacopocav.builder.internal.util.StringUtils.decapitalize;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

import com.github.jacopocav.builder.annotation.Builder;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class NameTemplateInterpolator {
    public String interpolate(String template, Class<?> targetClass) {
        return interpolate(template, fullSimpleNameOf(targetClass));
    }

    public String interpolate(String template, TypeElement targetType) {
        return interpolate(template, fullSimpleNameOf(targetType));
    }

    private String interpolate(String template, String fullSimpleName) {
        var result = template;

        if (template.contains(Builder.TARGET_CLASS_NAME)) {
            result = result.replace(Builder.TARGET_CLASS_NAME, fullSimpleName);
        }
        if (template.contains(Builder.LOWER_CASE_TARGET_CLASS_NAME)) {
            result = result.replace(Builder.LOWER_CASE_TARGET_CLASS_NAME, decapitalize(fullSimpleName));
        }

        return result;
    }

    private String fullSimpleNameOf(Class<?> targetClass) {
        if (targetClass.getEnclosingClass() == null) {
            return targetClass.getSimpleName();
        }

        return joinNestedSimpleNames(targetClass);
    }

    @SuppressWarnings("rawtypes")
    private String joinNestedSimpleNames(Class<?> targetClass) {
        return Stream.<Class>iterate(targetClass, Objects::nonNull, Class::getEnclosingClass)
                .map(Class::getSimpleName)
                .sorted(reversed())
                .collect(joining("_"));
    }

    private String fullSimpleNameOf(TypeElement sourceType) {
        if (sourceType.getNestingKind() == TOP_LEVEL) {
            return sourceType.getSimpleName().toString();
        }

        return joinNestedSimpleNames(sourceType);
    }

    private String joinNestedSimpleNames(TypeElement sourceType) {
        return Stream.iterate(sourceType, el -> isDeclaredType(el.getKind()), Element::getEnclosingElement)
                .map(Element::getSimpleName)
                .sorted(reversed())
                .collect(joining("_"));
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private static <T> Comparator<T> reversed() {
        return (a, b) -> -1;
    }
}
