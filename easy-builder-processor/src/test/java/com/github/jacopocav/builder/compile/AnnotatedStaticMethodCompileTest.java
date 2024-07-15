package com.github.jacopocav.builder.compile;

import static io.toolisticon.cute.JavaFileObjectUtils.readFromString;
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
import javax.tools.JavaFileObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnnotatedStaticMethodCompileTest {
    public static final String anotherPackage = "org.another.pkg";
    private static final String packageName = "org.example";
    private static final String classSimpleName = "SomeClass";
    private static final String classQualifiedName = packageName + "." + classSimpleName;
    private static final String staticMethodName = "someMethod";
    private static final String errorPositionTarget = staticMethodName + "(";
    private static final Type listOfIntegers = new GenericType<List<Integer>>() {}.getGenericType();

    private final BlackBoxTestSourceFilesInterface sut =
            Cute.blackBoxTest().given().processor(BuilderProcessor.class);

    @ParameterizedTest
    @MethodSource("validSources")
    void shouldCompile(List<JavaFileObject> sourceFiles, String builderPackage) {
        sut.andSourceFiles(sourceFiles.toArray(JavaFileObject[]::new))
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(builderPackage + "." + classSimpleName + "Builder")
                .testedSuccessfullyBy((builderClass, cuteClassLoader) -> {
                    var targetClass = cuteClassLoader.getClass(classQualifiedName);

                    BuilderAssert.assertThatBuilder(builderClass)
                            .withTargetClass(targetClass)
                            .withProperty(String.class, "someString")
                            .withProperty(long.class, "someLong")
                            .withProperty(listOfIntegers, "someListOfIntegers")
                            .isWellFormed()
                            .isWellBehavedUsingRecursiveComparison();
                })
                .executeTest();
    }

    @ParameterizedTest
    @MethodSource("invalidSources")
    void shouldNotCompile(String source, String className, String errorMessage) {
        var errorPosition = SourceUtils.firstPositionThatContains(source, errorPositionTarget);

        sut.andSourceFile(className, source)
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .atLine(errorPosition.line())
                .atColumn(errorPosition.column())
                .equals("@Builder processing error: %s".formatted(errorMessage))
                .executeTest();
    }

    static Stream<Arguments> validSources() {
        // language=java
        var staticFactoryInsideTargetClass =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            public class SomeClass {
                public String someString;
                public long someLong;
                public List<Integer> someListOfIntegers;

                @Builder
                public static SomeClass someMethod(String someString, long someLong, List<Integer> someListOfIntegers) {
                    var result = new SomeClass();
                    result.someString = someString;
                    result.someLong = someLong;
                    result.someListOfIntegers = someListOfIntegers;
                    return result;
                }
            }
        """;

        // language=java
        var staticFactoryOutsideTargetClass =
                """
            package org.example;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;

            class SomeClass {
                public String someString;
                public long someLong;
                public List<Integer> someListOfIntegers;
            }

            class FactoryMethods {
                @Builder
                public static SomeClass someMethod(String someString, long someLong, List<Integer> someListOfIntegers) {
                    var result = new SomeClass();
                    result.someString = someString;
                    result.someLong = someLong;
                    result.someListOfIntegers = someListOfIntegers;
                    return result;
                }
            }
        """;

        // language=java
        var targetClassInUsualPackage =
                """
            package org.example;
            import java.util.List;

            public class SomeClass {
                public String someString;
                public long someLong;
                public List<Integer> someListOfIntegers;
            }
        """;
        // language=java
        var staticFactoryInOtherPackage =
                """
            package org.another.pkg;
            import java.util.List;
            import com.github.jacopocav.builder.annotation.Builder;
            import org.example.SomeClass;

            public class FactoryMethods {
                @Builder
                public static SomeClass someMethod(String someString, long someLong, List<Integer> someListOfIntegers) {
                    var result = new SomeClass();
                    result.someString = someString;
                    result.someLong = someLong;
                    result.someListOfIntegers = someListOfIntegers;
                    return result;
                }
            }
        """;

        return Stream.of(
                arguments(List.of(readFromString(classQualifiedName, staticFactoryInsideTargetClass)), packageName),
                arguments(List.of(readFromString(classQualifiedName, staticFactoryOutsideTargetClass)), packageName),
                arguments(
                        List.of(
                                readFromString(classQualifiedName, targetClassInUsualPackage),
                                readFromString(anotherPackage + ".FactoryMethods", staticFactoryInOtherPackage)),
                        anotherPackage));
    }

    static Stream<Arguments> invalidSources() {
        // language=Java
        var instanceMethod =
                """
        package org.example;
        import com.github.jacopocav.builder.annotation.Builder;

        public class SomeClass {
            @Builder
            public SomeClass someMethod(String field) {
                return null;
            }
        }
        """;

        // language=Java
        var privateMethod =
                """
    package org.example;
    import com.github.jacopocav.builder.annotation.Builder;

    public class SomeClass {
        @Builder
        private static SomeClass someMethod(String field) {
            return null;
        }
    }
    """;

        // language=Java
        var zeroArgMethod =
                """
    package org.example;
    import com.github.jacopocav.builder.annotation.Builder;

    public class SomeClass {
        @Builder
        public static SomeClass someMethod() {
            return null;
        }
    }
    """;

        // language=Java
        var voidMethod =
                """
    package org.example;
    import com.github.jacopocav.builder.annotation.Builder;

    public class SomeClass {
        @Builder
        public static void someMethod(String parameter) {}
    }
    """;

        // language=Java
        var boxedVoidMethod =
                """
    package org.example;
    import com.github.jacopocav.builder.annotation.Builder;

    public class SomeClass {
        @Builder
        public static Void someMethod(String parameter) {
            return null;
        }
    }
    """;

        // language=Java
        var methodInsidePrivateClass =
                """
    package org.example;
    import com.github.jacopocav.builder.annotation.Builder;

    public class Container {
        private class SomeClass {
            @Builder
            public static SomeClass someMethod(String someString) {
            }
        }
    }
    """;

        return Stream.of(
                arguments(instanceMethod, classQualifiedName, "annotated method must be static"),
                arguments(privateMethod, classQualifiedName, "annotated element must not be private"),
                arguments(zeroArgMethod, classQualifiedName, "annotated method must have at least one argument"),
                arguments(voidMethod, classQualifiedName, "annotated method must not return void"),
                arguments(boxedVoidMethod, classQualifiedName, "annotated method must not return void"),
                arguments(
                        methodInsidePrivateClass,
                        packageName + ".Container",
                        "annotated element must not be enclosed in private class"));
    }
}
