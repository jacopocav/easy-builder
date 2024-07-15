package com.github.jacopocav.builder.compile;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.jacopocav.builder.processor.BuilderProcessor;
import com.github.jacopocav.builder.util.BuilderAssert;
import com.github.jacopocav.builder.util.GenericType;
import com.github.jacopocav.builder.util.SourceUtils;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi.BlackBoxTestSourceFilesInterface;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnnotatedClassCompileTest {
    private static final String packageName = "org.example";
    private static final String classSimpleName = "SomeClass";
    private static final String classQualifiedName = packageName + "." + classSimpleName;
    private static final String builderClass = classQualifiedName + "Builder";
    private static final String builderClassSimpleName = classSimpleName + "Builder";
    private static final String errorPositionTarget = "class " + classSimpleName;
    private static final Type listOfIntegers = new GenericType<List<Integer>>() {}.getGenericType();

    private final BlackBoxTestSourceFilesInterface sut =
            Cute.blackBoxTest().given().processor(BuilderProcessor.class);

    private static final String outerClassQualifiedName = packageName + ".OuterClass";
    private static final String innerClassQualifiedName = outerClassQualifiedName + "$" + classSimpleName;

    @Test
    void shouldCompileTopLevelClass() {
        // language=Java
        var source =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;
            import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

            @Builder(copyFactoryMethod = CopyFactoryMethodGeneration.DYNAMIC, setterPrefix = "{lowerCaseTargetClassName}_set")
            public class SomeClass {
                public SomeClass() {}

                public SomeClass(String someString, long someLong, List<Integer> someListOfIntegers) {
                }
            }
            """;

        sut.andSourceFile(classQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(classQualifiedName)
                .exists()
                .andThat()
                .generatedClass(builderClass)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(classQualifiedName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withSetterPrefix("{lowerCaseTargetClassName}_set")
                            .withProperty(String.class, "someString")
                            .withProperty(long.class, "someLong")
                            .withProperty(listOfIntegers, "someListOfIntegers")
                            .withoutDynamicCopyFactoryMethod()
                            .isWellFormed()
                            .isWellBehaved();
                })
                .executeTest();
    }

    @ParameterizedTest
    @MethodSource("innerClassSources")
    void shouldCompileInnerStaticClass(String source, String builderClassName, String metadataClassName) {
        sut.andSourceFile(outerClassQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(innerClassQualifiedName)
                .exists()
                .andThat()
                .generatedClass(builderClassName)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(innerClassQualifiedName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withClassName(metadataClassName)
                            .withProperty(String.class, "someString")
                            .withProperty(long.class, "someLong")
                            .withProperty(listOfIntegers, "someListOfIntegers")
                            .withoutCopyFactoryMethod()
                            .withSetterPrefix("with")
                            .isWellFormed()
                            .isWellBehaved();
                })
                .executeTest();
    }

    @ParameterizedTest
    @MethodSource("invalidSources")
    void shouldNotCompile(
            String source, String topClassQualifiedName, String errorMessage, String errorPositionTarget) {
        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(topClassQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals("@Builder processing error: " + errorMessage)
                .executeTest();
    }

    static Stream<Arguments> invalidSources() {
        // language=Java
        var classWithOnlyZeroArgConstructor =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public class SomeClass {
                public SomeClass() {
                }
            }
        """;
        // language=Java
        var classWithTooManyCandidateConstructors =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public class SomeClass {
                public SomeClass(String someString) {
                }
                public SomeClass(Long someLong, int someInt) {
                }
            }
        """;

        // language=Java
        var privateClass =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            public class OuterClass {
                @Builder
                private class SomeClass {
                    public SomeClass(String someString) {
                    }
                }
            }
        """;

        // language=Java
        var nonStaticInnerClass =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            public class OuterClass {
                @Builder
                class SomeClass {
                    public SomeClass(String someString) {
                    }
                }
            }
        """;

        // language=Java
        var classWithPrivateConstructor =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public class SomeClass {
                private SomeClass(String someString) {
                }
            }
        """;

        // language=Java
        var classWithoutAccessor =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            @Builder
            public class SomeClass {
                public SomeClass(String someString) {
                }
            }
        """;

        return Stream.of(
                arguments(
                        classWithOnlyZeroArgConstructor,
                        classQualifiedName,
                        "no valid constructor found. A non-private constructor with at least 1 argument must be present",
                        errorPositionTarget),
                arguments(
                        classWithTooManyCandidateConstructors,
                        classQualifiedName,
                        "found multiple non-private constructors with at least 1 argument. Move the @Builder annotation to the constructor you want the builder to use",
                        errorPositionTarget),
                arguments(
                        privateClass,
                        outerClassQualifiedName,
                        "annotated element must not be private",
                        errorPositionTarget),
                arguments(
                        nonStaticInnerClass,
                        outerClassQualifiedName,
                        "annotated class must be static",
                        errorPositionTarget),
                arguments(
                        classWithPrivateConstructor,
                        classQualifiedName,
                        "no valid constructor found. A non-private constructor with at least 1 argument must be present",
                        errorPositionTarget),
                arguments(
                        classWithoutAccessor,
                        classQualifiedName,
                        "could not find any accessor (getter or field) for parameter someString. Add it or disable static copy method generation with copyFactoryMethod=DISABLED or copyFactoryMethod=DYNAMIC",
                        "someString)"));
    }

    static Stream<Arguments> innerClassSources() {
        // language=Java
        var template =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;
            import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

            public %s OuterClass%s { %s
                @Builder(className = "%s", setterPrefix = "with", factoryMethodName = "create", buildMethodName = "build", copyFactoryMethod = CopyFactoryMethodGeneration.DISABLED)
                public static class SomeClass {
                    public SomeClass() {}

                    public SomeClass(String someString, long someLong, List<Integer> someListOfIntegers) {
                    }
                }
            }
        """;

        var insideClass = template.formatted("class", "", "", builderClassSimpleName);
        var insideEnum = template.formatted("enum", "", "A_CONSTANT;", builderClassSimpleName);
        var insideRecord = template.formatted("record", "(String someString)", "", builderClassSimpleName);

        var interfaceTemplate = template.replace("public static class", "class");
        var insideInterface = interfaceTemplate.formatted("interface", "", "", builderClassSimpleName);
        var insideAnnotationType = interfaceTemplate.formatted("@interface", "", "", builderClassSimpleName);

        var withDefaultName = template.formatted("class", "", "", "ABuilderOf{TargetClassName}");

        return Stream.of(
                arguments(insideClass, builderClass, builderClassSimpleName),
                arguments(insideEnum, builderClass, builderClassSimpleName),
                arguments(insideRecord, builderClass, builderClassSimpleName),
                arguments(insideInterface, builderClass, builderClassSimpleName),
                arguments(insideAnnotationType, builderClass, builderClassSimpleName),
                arguments(
                        withDefaultName,
                        packageName + ".ABuilderOfOuterClass" + "_" + classSimpleName,
                        "ABuilderOf{TargetClassName}"));
    }
}
