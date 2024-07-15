package com.github.jacopocav.builder.compile;

import com.github.jacopocav.builder.util.BuilderAssert;
import com.github.jacopocav.builder.util.SourceUtils;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import com.github.jacopocav.builder.util.GenericType;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi.BlackBoxTestSourceFilesInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Type;
import java.util.List;

class BuilderOptionsCompileTest {
    private static final String packageName = "org.example";
    private static final String recordSimpleName = "SomeRecord";
    private static final String recordQualifiedName = packageName + "." + recordSimpleName;
    public static final String builderQualifiedName = recordQualifiedName + "Builder";
    private static final String errorPositionTarget = "record " + recordSimpleName;
    private static final Type listOfIntegers = new GenericType<List<Integer>>() {}.getGenericType();

    private final BlackBoxTestSourceFilesInterface sut =
            Cute.blackBoxTest().given().processor(BuilderProcessor.class);

    @Test
    void shouldCompileGivenValidOptions() {
        // language=Java
        var source =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder(factoryMethodName = "newBuilder")
            public record SomeRecord(String someString, long someLong, List<Integer> someListOfIntegers) {}
            """;

        sut.andSourceFile(recordQualifiedName, source)
                .andUseCompilerOptions(
                        "-Abuilder.setterPrefix=set",
                        "-Abuilder.factoryMethodName=giveMeNewBuilder",
                        "-Abuilder.buildMethodName=construct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(recordQualifiedName)
                .exists()
                .andThat()
                .generatedClass(builderQualifiedName)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var sourceClass = cuteClassLoader.getClass(recordQualifiedName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withSourceClass(sourceClass)
                            .withProperty(String.class, "someString")
                            .withProperty(long.class, "someLong")
                            .withProperty(listOfIntegers, "someListOfIntegers")
                            .withSetterPrefix("set")
                            .withBuildMethodName("construct")
                            .withFactoryMethodName("newBuilder")
                            .isWellFormed()
                            .isWellBehaved();
                })
                .executeTest();
    }

    @ParameterizedTest
    @CsvSource({
        "builder.setterPrefix,invalid-setter-prefix",
        "builder.factoryMethodName,enum",
        "builder.buildMethodName,1buildMeThis",
    })
    void shouldNotCompileGivenInvalidCompilerOption(String optionName, String optionValue) {
        // language=Java
        var source =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public record SomeRecord() {}
            """;

        sut.andSourceFile(recordQualifiedName, source)
                .andUseCompilerOptions("-A%s=%s".formatted(optionName, optionValue))
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .contains(
                        "@Builder processing error: value \"%s\" of compiler argument %s is not a permitted Java identifier"
                                .formatted(optionValue, optionName))
                .executeTest();
    }

    @ParameterizedTest
    @CsvSource({
        "setterPrefix,invalid-setter-prefix",
        "factoryMethodName,enum",
        "buildMethodName,1buildMeThis",
    })
    void shouldNotCompileGivenInvalidAnnotationOption(String optionName, String optionValue) {
        // language=Java
        var source =
                """
        package org.example;
        import com.github.jacopocav.builder.annotation.Builder;

        @Builder(%s = "%s")
        public record SomeRecord() {}
        """
                        .formatted(optionName, optionValue);

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(recordQualifiedName, source)
                .andUseCompilerOptions("-A%s=%s".formatted(optionName, optionValue))
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .contains("@Builder processing error: value \"%s\" of attribute %s is not a permitted Java identifier"
                        .formatted(optionValue, optionName))
                .executeTest();
    }
}
