package com.github.jacopocav.builder.util;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;
import com.github.jacopocav.builder.annotation.Builder.Defaults;
import com.github.jacopocav.builder.annotation.GeneratedBuilder;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.processing.generation.name.NameTemplateInterpolator;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.github.jacopocav.builder.internal.util.StringUtils.composeSetterName;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static javax.lang.model.element.Modifier.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Assert class for generated builder classes.
 */
public class BuilderAssert extends AbstractRichClassAssert<BuilderAssert> {
    private static final NameTemplateInterpolator interpolator = new NameTemplateInterpolator();

    private record Property(Type type, String name) {}

    private static final RecursiveComparisonConfiguration DEFAULT_RECURSIVE_COMPARISON_CONFIG =
            new RecursiveComparisonConfiguration();

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private final Class<?> sourceClass;
    private String className = Defaults.CLASS_NAME;
    private String factoryMethodName = Defaults.FACTORY_METHOD_NAME;
    private CopyFactoryMethodGeneration copyFactoryMethod = Defaults.COPY_FACTORY_METHOD;
    private String copyFactoryMethodName = Defaults.COPY_FACTORY_METHOD_NAME;
    private String setterPrefix = Defaults.SETTER_PREFIX;
    private String buildMethodName = Defaults.BUILD_METHOD_NAME;
    private final List<Property> properties = new ArrayList<>();

    BuilderAssert(Class<?> builderClass, Class<?> sourceClass) {
        super(builderClass, BuilderAssert.class);
        this.sourceClass = sourceClass;
    }

    public static BuilderAssertBuilder assertThatBuilder(Class<?> builderClass) {
        return new BuilderAssertBuilder(builderClass);
    }

    public BuilderAssert withClassName(String className) {
        this.className = className;
        return myself;
    }

    public BuilderAssert withFactoryMethodName(String staticFactoryName) {
        this.factoryMethodName = requireNonNull(staticFactoryName);
        return myself;
    }

    public BuilderAssert withLenientCopyFactoryMethodName(String staticFromMethodName) {
        this.copyFactoryMethod = CopyFactoryMethodGeneration.DYNAMIC;
        this.copyFactoryMethodName = requireNonNull(staticFromMethodName);
        return myself;
    }

    public BuilderAssert withStrictCopyFactoryMethodName(String staticFromMethodName) {
        this.copyFactoryMethod = CopyFactoryMethodGeneration.ENABLED;
        this.copyFactoryMethodName = requireNonNull(staticFromMethodName);
        return myself;
    }

    public BuilderAssert withoutCopyFactoryMethod() {
        this.copyFactoryMethod = CopyFactoryMethodGeneration.DISABLED;
        return myself;
    }

    public BuilderAssert withoutDynamicCopyFactoryMethod() {
        this.copyFactoryMethod = CopyFactoryMethodGeneration.DYNAMIC;
        return myself;
    }

    public BuilderAssert withBuildMethodName(String buildMethodName) {
        this.buildMethodName = requireNonNull(buildMethodName);
        return myself;
    }

    public BuilderAssert withSetterPrefix(String setterPrefix) {
        this.setterPrefix = requireNonNullElse(setterPrefix, "");
        return myself;
    }

    public BuilderAssert withProperty(Type type, String name) {
        this.properties.add(new Property(requireNonNull(type), requireNonNull(name)));
        return myself;
    }

    /**
     * Asserts that the builder class has the expected structure:
     * <ul>
     *     <li>It must be public
     *     <li>It must be annotated with {@link com.github.jacopocav.builder.annotation.GeneratedBuilder}
     *     <li>It must have a private zero-arguments constructor
     *     <li>It must have a public build method (with the name specified with {@link #withBuildMethodName(String)})
     *     <li>It must have a public static factory method (with the name specified with {@link #withFactoryMethodName(String)})
     *     <li>For every property passed with {@link #withProperty(Type, String)}:
     *     <ul>
     *         <li>It must have a private field with the same {@code type} and {@code name}
     *         <li>It must have a public setter (with the name composed by joining the setter prefix
     *         specified with {@link #withSetterPrefix(String)} with the property {@code name})
     *     </ul>
     *     <li>If not disabled with {@link #withoutCopyFactoryMethod()},
     *     it must have a public static copy method (with the name specified with {@link #withStrictCopyFactoryMethodName(String)})
     * </ul>
     */
    public BuilderAssert isWellFormed() {
        isPublic();
        hasAnnotation(GeneratedBuilder.class);
        assertMetadataAnnotationsMatchOptions();
        hasDeclaredConstructor(PRIVATE);
        hasDeclaredMethod(PUBLIC, sourceClass, interpolator.interpolate(buildMethodName, sourceClass));
        hasDeclaredMethod(Set.of(PUBLIC, STATIC), actual, interpolator.interpolate(factoryMethodName, sourceClass));

        if (copyFactoryMethod == CopyFactoryMethodGeneration.ENABLED) {
            hasDeclaredMethod(
                    Set.of(PUBLIC, STATIC),
                    actual,
                    interpolator.interpolate(copyFactoryMethodName, sourceClass),
                    sourceClass);
        }

        Assertions.assertThat(properties).allSatisfy(property -> {
            hasDeclaredField(PRIVATE, property.type(), property.name());
            hasDeclaredMethod(
                    PUBLIC,
                    actual,
                    composeSetterName(interpolator.interpolate(setterPrefix, sourceClass), property.name()),
                    property.type());
        });

        return myself;
    }

    private void assertMetadataAnnotationsMatchOptions() {
        var builderOptionsMap =
                BuilderOption.all().stream().collect(toUnmodifiableMap(BuilderOption::annotationName, identity()));
        var generatedBuilderAnnotation = actual.getAnnotation(GeneratedBuilder.class);
        var generatedBuilderAttributes = Arrays.stream(GeneratedBuilder.class.getDeclaredMethods())
                .collect(toUnmodifiableMap(Method::getName, m -> getUnchecked(m, generatedBuilderAnnotation)));

        Assertions.assertThat(builderOptionsMap.keySet()).isEqualTo(generatedBuilderAttributes.keySet());

        Assertions.assertThat(builderOptionsMap).allSatisfy((name, option) -> {
            Object expected =
                    switch (option) {
                        case SETTER_PREFIX -> setterPrefix;
                        case BUILD_METHOD_NAME -> buildMethodName;
                        case CLASS_NAME -> className;
                        case FACTORY_METHOD_NAME -> factoryMethodName;
                        case COPY_FACTORY_METHOD_NAME -> copyFactoryMethodName;
                        case COPY_FACTORY_METHOD -> copyFactoryMethod;
                    };

            Assertions.assertThat(generatedBuilderAttributes).containsEntry(name, expected);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getUnchecked(Method accessor, Object instance) {
        try {
            return (T) accessor.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that the builder class behaves correctly, as follows:
     * <ul>
     *     <li>It creates a new builder instance by calling the
     *     {@link #withFactoryMethodName(String) static factory method}.
     *     <li>For every {@link #withProperty(Type, String) property}, it calls the associated
     *     setter passing a random value.
     *     <li>It calls the {@link #withBuildMethodName(String) build method} to create the final object
     *     <li>It asserts that the object is not null.
     * </ul>
     * Additionally, if the {@link #withStrictCopyFactoryMethodName(String) static copy method} has not been
     * explicitly disabled with {@link #withoutCopyFactoryMethod()}:
     * <ul>
     *     <li>It calls the static copy method, passing the previously created object.
     *     <li>It asserts that the returned builder is equal to the first one, using
     *     {@link #usingRecursiveComparison() recursive comparison}.
     *     <li>It calls the build method to create a new object.
     *     <li>It asserts that the newly created object is equal to the previously created one.
     * </ul>
     */
    public BuilderAssert isWellBehaved() {
        return isWellBehaved(false, null);
    }

    /**
     * Variant of {@link #isWellBehaved()} that uses {@link #usingRecursiveComparison() recursive comparison}
     * to compare the objects created by the builder.
     */
    public BuilderAssert isWellBehavedUsingRecursiveComparison() {
        return isWellBehavedUsingRecursiveComparison(DEFAULT_RECURSIVE_COMPARISON_CONFIG);
    }

    /**
     * Variant of {@link #isWellBehaved()} that uses {@link #usingRecursiveComparison() recursive comparison}
     * to compare the objects created by the builder.
     *
     * @param configuration the custom recursive comparison configuration
     */
    public BuilderAssert isWellBehavedUsingRecursiveComparison(RecursiveComparisonConfiguration configuration) {
        return isWellBehaved(true, configuration);
    }

    public BuilderAssert isWellBehaved(boolean useRecursiveComparison, RecursiveComparisonConfiguration configuration) {
        try {
            var createMethod = actual.getMethod(interpolator.interpolate(factoryMethodName, sourceClass));
            var builder = createMethod.invoke(null);

            for (final Property property : properties) {
                var setter = actual.getMethod(
                        composeSetterName(interpolator.interpolate(setterPrefix, sourceClass), property.name()),
                        TypeUtils.getClass(property.type()));
                var randomValue = PODAM_FACTORY.manufacturePojoWithFullData(
                        TypeUtils.getClass(property.type()), TypeUtils.getTypeArguments(property.type()));

                setter.invoke(builder, randomValue);
            }

            var buildMethod = actual.getMethod(interpolator.interpolate(buildMethodName, sourceClass));
            var builtValue = buildMethod.invoke(builder);

            Assertions.assertThat(builtValue).isNotNull();

            if (copyFactoryMethod == CopyFactoryMethodGeneration.ENABLED) {
                var fromMethod =
                        actual.getMethod(interpolator.interpolate(copyFactoryMethodName, sourceClass), sourceClass);
                var copiedBuilder = fromMethod.invoke(null, builtValue);
                var copiedValue = buildMethod.invoke(copiedBuilder);

                Assertions.assertThat(copiedBuilder).usingRecursiveComparison().isEqualTo(builder);
                if (useRecursiveComparison) {
                    Assertions.assertThat(copiedValue)
                            .usingRecursiveComparison(configuration)
                            .isEqualTo(builtValue);
                } else {
                    Assertions.assertThat(copiedValue).isEqualTo(builtValue);
                }
            }

            return myself;
        } catch (ReflectiveOperationException e) {
            return fail(e);
        }
    }
}
