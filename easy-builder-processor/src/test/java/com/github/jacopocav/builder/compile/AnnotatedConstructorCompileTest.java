package com.github.jacopocav.builder.compile;

import com.github.jacopocav.builder.util.BuilderAssert;
import com.github.jacopocav.builder.util.GenericType;
import com.github.jacopocav.builder.util.SourceUtils;
import com.github.jacopocav.builder.processor.BuilderProcessor;
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

class AnnotatedConstructorCompileTest {
    private static final String packageName = "org.example";
    private static final String classSimpleName = "SomeClass";
    private static final String classQualifiedName = packageName + "." + classSimpleName;
    private static final String errorPositionTarget = classSimpleName + "(";
    private static final Type listOfExtendsInteger = new GenericType<List<? extends Integer>>() {}.getGenericType();

    private final BlackBoxTestSourceFilesInterface sut =
            Cute.blackBoxTest().given().processor(BuilderProcessor.class);

    @ParameterizedTest
    @MethodSource("compilableSources")
    void shouldCompile(String source, String targetClassName, String builderClassName) {
        sut.andSourceFile(classQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(classQualifiedName)
                .exists()
                .andThat()
                .generatedClass(builderClassName)
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(targetClassName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withProperty(String.class, "someString")
                            .withProperty(Long.class, "someLong")
                            .withProperty(listOfExtendsInteger, "someList")
                            .withStrictCopyFactoryMethodName("copyFrom")
                            .isWellFormed()
                            .isWellBehavedUsingRecursiveComparison();
                })
                .executeTest();
    }

    private static Stream<Arguments> compilableSources() {
        // language=Java
        var topLevelClass =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            public class SomeClass {
                String someString;
                long someLong;
                List<Integer> someList;

                public SomeClass() {}

                @Builder(copyFactoryMethodName = "copyFrom")
                public SomeClass(String someString, Long someLong, List<? extends Integer> someList) {
                    this.someString = someString;
                    this.someLong = someLong;
                    this.someList = List.copyOf(someList);
                }

                public SomeClass(int someInt, String[] someStringArray) {
                }
            }
            """;

        // language=Java
        var nestedClass =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            public class SomeClass {
                public static class InnerClass {
                    String someString;
                    long someLong;
                    List<Integer> someList;

                    public InnerClass() {}

                    @Builder(copyFactoryMethodName = "copyFrom")
                    public InnerClass(String someString, Long someLong, List<? extends Integer> someList) {
                        this.someString = someString;
                        this.someLong = someLong;
                        this.someList = List.copyOf(someList);
                    }

                    public InnerClass(int someInt, String[] someStringArray) {
                    }
                }
            }

            """;

        return Stream.of(
                arguments(topLevelClass, classQualifiedName, classQualifiedName + "Builder"),
                arguments(nestedClass, classQualifiedName + "$InnerClass", classQualifiedName + "_InnerClassBuilder"));
    }

    @Test
    void shouldNotCompileZeroArgConstructor() {
        // language=Java
        var source =
                """
                package org.example;
                import com.github.jacopocav.builder.annotation.Builder;

                public class SomeClass {
                    @Builder
                    public SomeClass() {
                    }
                }
                """;

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(classQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals("@Builder processing error: annotated constructor must have at least one argument")
                .executeTest();
    }

    @Test
    void shouldNotCompileConstructorInsideAbstractClass() {
        // language=Java
        var source =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            public abstract class SomeClass {
                @Builder
                public SomeClass(String field) {
                }
            }
            """;

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(classQualifiedName, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals("@Builder processing error: class of annotated constructor must not be abstract")
                .executeTest();
    }

    @Test
    void shouldNotCompileConstructorInsidePrivateClass() {
        var containerClass = packageName + ".Container";
        // language=Java
        var source =
                """
            package org.example;
            import com.github.jacopocav.builder.annotation.Builder;

            public class Container {
                private class SomeClass {
                    @Builder
                    public SomeClass(String someString) {
                    }
                }
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
                .equals("@Builder processing error: annotated element must not be enclosed in private class")
                .executeTest();
    }

    @Test
    void shouldNotCompileClassWithPrivateConstructor() {
        // language=Java
        var source =
                """
        package org.example;
        import com.github.jacopocav.builder.annotation.Builder;

        public class SomeClass {
            @Builder
            private SomeClass(String someString) {
            }
        }
        """;

        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(classQualifiedName, source)
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
