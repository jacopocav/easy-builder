package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder;

import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.parseBoolean;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

public class OptionsRepositoryImpl implements OptionsRepository {
    private final Options defaults;

    public OptionsRepositoryImpl(Map<String, String> compilerOptions) {
        this.defaults = Options.builder()
                .withSetterPrefix(
                        compilerOptions.getOrDefault(
                                BuilderOption.SETTER_PREFIX.compilerName(), BuilderOption.SETTER_PREFIX.defaultValue()))
                .withBuildMethodName(compilerOptions.getOrDefault(
                        BuilderOption.BUILD_METHOD_NAME.compilerName(), BuilderOption.BUILD_METHOD_NAME.defaultValue()))
                .withStaticFactoryName(compilerOptions.getOrDefault(
                        BuilderOption.STATIC_FACTORY_NAME.compilerName(), BuilderOption.STATIC_FACTORY_NAME.defaultValue()))
                .withGenerateStaticFromMethod(parseBoolean(compilerOptions.getOrDefault(
                        BuilderOption.COPY_FACTORY_METHOD.compilerName(),
                        BuilderOption.COPY_FACTORY_METHOD.defaultValue().toString())))
                .withStaticFromMethodName(compilerOptions.getOrDefault(
                        BuilderOption.COPY_FACTORY_METHOD_NAME.compilerName(), BuilderOption.COPY_FACTORY_METHOD_NAME.defaultValue()))
                .build();
    }

    @Override
    public Options get(Element element) {
        var builderAnnotation = element.getAnnotationMirrors().stream()
                .filter(annotation -> annotation.getAnnotationType().toString().equals(Builder.class.getName()))
                .findFirst()
                .orElseThrow();

        var attributes = builderAnnotation.getElementValues().entrySet().stream()
                .collect(toMap(e -> e.getKey().getSimpleName().toString(), e -> e.getValue()
                        .getValue()));

        return Options.builder()
                .withClassName(Optional.ofNullable(getAttribute(attributes, BuilderOption.CLASS_NAME.annotationName(), ""))
                        .filter(not(String::isEmpty)))
                .withSetterPrefix(getAttribute(attributes, BuilderOption.SETTER_PREFIX.annotationName(), defaults.setterPrefix()))
                .withBuildMethodName(
                        getAttribute(attributes, BuilderOption.BUILD_METHOD_NAME.annotationName(), defaults.buildMethodName()))
                .withStaticFactoryName(
                        getAttribute(attributes, BuilderOption.STATIC_FACTORY_NAME.annotationName(), defaults.staticFactoryName()))
                .withGenerateStaticFromMethod(getAttribute(
                        attributes,
                        BuilderOption.COPY_FACTORY_METHOD.annotationName(),
                        BuilderOption.COPY_FACTORY_METHOD.defaultValue()))
                .withStaticFromMethodName(getAttribute(
                        attributes, BuilderOption.COPY_FACTORY_METHOD_NAME.annotationName(), BuilderOption.COPY_FACTORY_METHOD_NAME.defaultValue()))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAttribute(Map<String, Object> attributes, String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }
}
