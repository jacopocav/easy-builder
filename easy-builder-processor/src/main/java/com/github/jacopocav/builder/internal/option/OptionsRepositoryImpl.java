package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;
import com.github.jacopocav.builder.processing.generation.name.NameTemplateInterpolator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public class OptionsRepositoryImpl implements OptionsRepository {
    private final RawOptions defaults;
    private final NameTemplateInterpolator nameTemplateInterpolator;

    public OptionsRepositoryImpl(
            Map<String, String> compilerOptions, NameTemplateInterpolator nameTemplateInterpolator) {
        this.defaults = RawOptions.builder()
                .className(compilerOptions.getOrDefault(
                        BuilderOption.CLASS_NAME.compilerName(), BuilderOption.CLASS_NAME.defaultValue()))
                .setterPrefix(compilerOptions.getOrDefault(
                        BuilderOption.SETTER_PREFIX.compilerName(), BuilderOption.SETTER_PREFIX.defaultValue()))
                .buildMethodName(compilerOptions.getOrDefault(
                        BuilderOption.BUILD_METHOD_NAME.compilerName(), BuilderOption.BUILD_METHOD_NAME.defaultValue()))
                .staticFactoryName(compilerOptions.getOrDefault(
                        BuilderOption.FACTORY_METHOD_NAME.compilerName(),
                        BuilderOption.FACTORY_METHOD_NAME.defaultValue()))
                .copyFactoryMethod(CopyFactoryMethodGeneration.valueOf(compilerOptions.getOrDefault(
                        BuilderOption.COPY_FACTORY_METHOD.compilerName(),
                        BuilderOption.COPY_FACTORY_METHOD.defaultValue().toString())))
                .copyFactoryMethodName(compilerOptions.getOrDefault(
                        BuilderOption.COPY_FACTORY_METHOD_NAME.compilerName(),
                        BuilderOption.COPY_FACTORY_METHOD_NAME.defaultValue()))
                .build();
        this.nameTemplateInterpolator = nameTemplateInterpolator;
    }

    @Override
    public RawOptions getRaw(Element annotatedElement) {
        var builderAnnotation = annotatedElement.getAnnotationMirrors().stream()
                .filter(annotation -> annotation.getAnnotationType().toString().equals(Builder.class.getName()))
                .findFirst()
                .orElseThrow();

        var attributes = builderAnnotation.getElementValues().entrySet().stream()
                .collect(toMap(e -> e.getKey().getSimpleName().toString(), e -> e.getValue()
                        .getValue()));

        return RawOptions.builder()
                .className(getAttribute(attributes, BuilderOption.CLASS_NAME.annotationName(), defaults.className()))
                .setterPrefix(
                        getAttribute(attributes, BuilderOption.SETTER_PREFIX.annotationName(), defaults.setterPrefix()))
                .buildMethodName(getAttribute(
                        attributes, BuilderOption.BUILD_METHOD_NAME.annotationName(), defaults.buildMethodName()))
                .staticFactoryName(getAttribute(
                        attributes, BuilderOption.FACTORY_METHOD_NAME.annotationName(), defaults.staticFactoryName()))
                .copyFactoryMethod(
                        Optional.ofNullable(attributes.get(BuilderOption.COPY_FACTORY_METHOD.annotationName()))
                                .map(VariableElement.class::cast)
                                .map(VariableElement::getSimpleName)
                                .map(Name::toString)
                                .map(CopyFactoryMethodGeneration::valueOf)
                                .orElse(defaults.copyFactoryMethod()))
                .copyFactoryMethodName(getAttribute(
                        attributes,
                        BuilderOption.COPY_FACTORY_METHOD_NAME.annotationName(),
                        defaults.copyFactoryMethodName()))
                .build();
    }

    @Override
    public InterpolatedOptions getInterpolated(RawOptions rawOptions, TypeElement enclosingType) {
        return InterpolatedOptions.builder()
                .raw(rawOptions)
                .className(nameTemplateInterpolator.interpolate(rawOptions.className(), enclosingType))
                .setterPrefix(nameTemplateInterpolator.interpolate(rawOptions.setterPrefix(), enclosingType))
                .buildMethodName(nameTemplateInterpolator.interpolate(rawOptions.buildMethodName(), enclosingType))
                .staticFactoryName(nameTemplateInterpolator.interpolate(rawOptions.staticFactoryName(), enclosingType))
                .copyFactoryMethod(rawOptions.copyFactoryMethod())
                .copyFactoryMethodName(
                        nameTemplateInterpolator.interpolate(rawOptions.copyFactoryMethodName(), enclosingType))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(Map<String, Object> attributes, String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }
}
