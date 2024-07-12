package com.github.jacopocav.builder;

import com.github.jacopocav.builder.Builder.Defaults;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.assertj.core.api.Assertions.assertThat;

class BuilderDefaultsTest {
    private static final Map<String, Field> CONSTANTS = Arrays.stream(Defaults.class.getDeclaredFields())
            .collect(toUnmodifiableMap(field -> UPPER_UNDERSCORE.to(LOWER_CAMEL, field.getName()), identity()));

    @ParameterizedTest
    @MethodSource("builderMethods")
    void annotationDefaultsShouldMatchConstants(Method builderMethod) throws IllegalAccessException {
        assertThat(CONSTANTS)
                .containsKey(builderMethod.getName());

        assertThat(builderMethod.getDefaultValue())
                .isEqualTo(CONSTANTS.get(builderMethod.getName()).get(null));
    }

    private static Stream<Arguments> builderMethods() {
        return Stream.of(Builder.class.getDeclaredMethods()).map(Arguments::of);
    }
}
