package com.github.jacopocav.builder.compile;

import com.github.jacopocav.builder.util.BuilderAssert;
import com.github.jacopocav.builder.util.Generated;
import com.github.jacopocav.builder.util.SourceUtils;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import com.github.jacopocav.builder.util.GenericType;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi.BlackBoxTestSourceFilesInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class AnnotatedRecordCompileTest {
    private static final String packageName = "org.example";
    private static final String recordSimpleName = "SomeRecord";
    private static final String recordQualifiedName = packageName + "." + recordSimpleName;
    private static final String errorPositionTarget = "record " + recordSimpleName;
    private static final Type listOfIntegers = new GenericType<List<Integer>>() {}.getGenericType();

    private final BlackBoxTestSourceFilesInterface sut =
            Cute.blackBoxTest().given().processor(BuilderProcessor.class);

    @ParameterizedTest
    @MethodSource("compilableRecords")
    void shouldCompile(String source, String targetClassName, String builderClassName) {
        sut.andSourceFile(recordQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(recordQualifiedName)
                .exists()
                .andThat()
                .generatedClass(builderClassName)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(targetClassName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withProperty(String.class, "someString")
                            .withProperty(long.class, "someLong")
                            .withProperty(listOfIntegers, "someListOfIntegers")
                            .isWellFormed()
                            .isWellBehaved();
                })
                .executeTest();
    }

    @Test
    void shouldCompileWithTypeClashes() {
        // language=Java
        var source =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public record Metadata( // clash with @Metadata
                java.sql.Types sqlTypes,
                javax.lang.model.util.Types javaxTypes,
                List<String> someListOfStrings,
                com.github.jacopocav.builder.util.Generated clashingGenerated
            ) {}
            """;

        var className = packageName + ".Metadata";
        var builderClassName = className + "Builder";
        var listOfStrings = new GenericType<List<String>>() {}.getGenericType();

        sut.andSourceFile(className, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(className)
                .exists()
                .andThat()
                .generatedClass(builderClassName)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(className);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withProperty(java.sql.Types.class, "sqlTypes")
                            .withProperty(javax.lang.model.util.Types.class, "javaxTypes")
                            .withProperty(listOfStrings, "someListOfStrings")
                            .withProperty(Generated.class, "clashingGenerated")
                            .isWellFormed();
                })
                .executeTest();
    }

    private static Stream<Arguments> compilableRecords() {
        // language=Java
        var topLevelRecord =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public record SomeRecord(String someString, long someLong, List<Integer> someListOfIntegers) {}
            """;

        // language=Java
        var nestedRecord =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            public record SomeRecord(boolean someBoolean) {
                @Builder
                public record NestedRecord(String someString, long someLong, List<Integer> someListOfIntegers) {}
            }
            """;

        return Stream.of(
                arguments(topLevelRecord, recordQualifiedName, recordQualifiedName + "Builder"),
                arguments(
                        nestedRecord,
                        recordQualifiedName + "$NestedRecord",
                        recordQualifiedName + "_NestedRecordBuilder"));
    }

    @Test
    void shouldNotCompileRecordWithZeroComponents() {
        // language=Java
        var source =
                """
                package org.example;
                import com.github.jacopocav.builder.annotation.Builder;

                @Builder
                public record SomeRecord() {}
                """;

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(recordQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals(
                        "@Builder processing error: no valid constructor found. A non-private constructor with at least 1 argument must be present")
                .executeTest();
    }

    @Test
    void shouldNotCompilePrivateRecord() {
        var containerClass = packageName + ".Container";
        // language=Java
        var source =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            public class Container {
                @Builder
                private record SomeRecord() {}
            }
            """;

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(containerClass, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals("@Builder processing error: annotated element must not be private")
                .executeTest();
    }
}
