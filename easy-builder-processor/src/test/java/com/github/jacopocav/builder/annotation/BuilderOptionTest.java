package com.github.jacopocav.builder.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class BuilderOptionTest {

    @ParameterizedTest
    @EnumSource
    void annotationNameShouldBeEqualToAnnotationMethodName(BuilderOption option) {
        assertThatCode(() -> Builder.class.getDeclaredMethod(option.annotationName()))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource
    void compilerArgNameShouldHaveCommonPrefix(BuilderOption option) {
        assertThat(option.compilerName()).startsWith("easy.builder.");
    }

    @ParameterizedTest
    @EnumSource(names = "SETTER_PREFIX", mode = EXCLUDE)
    void shouldNotBePrefix(BuilderOption sut) {
        assertThat(sut.isPrefix()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(names = "SETTER_PREFIX")
    void shouldBePrefix(BuilderOption sut) {
        assertThat(sut.isPrefix()).isTrue();
    }

    @Test
    void attributeConstantsShouldMatchNumberOfAnnotationMethods() {
        assertThat(Builder.class.getDeclaredMethods()).hasSameSizeAs(BuilderOption.all());
    }

    @ParameterizedTest
    @MethodSource("optionsWithDefaultValueTypes")
    void defaultValueShouldHaveExpectedType(BuilderOption sut, Class<?> expectedDefaultValueType) {
        // when
        var result = sut.defaultValue();

        // then
        assertThat(result).isInstanceOf(expectedDefaultValueType);
    }

    static Stream<Arguments> optionsWithDefaultValueTypes() {
        return BuilderOption.all().stream().map(option -> {
            var type =
                    switch (option) {
                        case CLASS_NAME,
                                SETTER_PREFIX,
                                BUILD_METHOD_NAME,
                                FACTORY_METHOD_NAME,
                                COPY_FACTORY_METHOD_NAME -> String.class;
                        case COPY_FACTORY_METHOD -> CopyFactoryMethodGeneration.class;
                    };
            return arguments(option, type);
        });
    }
}
