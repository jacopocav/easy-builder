package com.github.jacopocav.builder.processor;

import static com.github.jacopocav.builder.processing.error.AggregatedProcessingException.processingExceptions;
import static com.github.jacopocav.builder.processing.error.ProcessingException.processingException;
import static io.toolisticon.cute.Cute.unitTest;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.option.OptionCompilerArgumentsValidator;
import com.github.jacopocav.builder.processing.error.ProcessingException;
import com.github.jacopocav.builder.processing.error.printer.ProcessingExceptionPrinter;
import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import com.github.jacopocav.builder.processing.generation.SingleElementJavaFileGenerator;
import com.github.jacopocav.builder.processing.writer.GeneratedJavaFileWriter;
import com.github.jacopocav.builder.util.mock.ContextMock;
import io.toolisticon.cute.PassIn;
import io.toolisticon.cute.UnitTest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@ExtendWith({MockitoExtension.class})
class BuilderProcessorTest {
    private static final PodamFactory PODAM = new PodamFactoryImpl();

    @Mock
    private TypeElement annotation;

    @Mock
    private RoundEnvironment roundEnvironment;

    @Mock
    private SingleElementJavaFileGenerator singleElementFileGenerator;

    @Mock
    private OptionCompilerArgumentsValidator optionCompilerArgumentsValidator;

    @Mock
    private ProcessingExceptionPrinter processingExceptionPrinter;

    @Mock
    private GeneratedJavaFileWriter generatedJavaFileWriter;

    private BuilderProcessor sut;

    @BeforeEach
    void setUp() {
        sut = new BuilderProcessor(new ContextMock(
                singleElementFileGenerator,
                optionCompilerArgumentsValidator,
                generatedJavaFileWriter,
                processingExceptionPrinter));
    }

    @Test
    void shouldSupportBuilderAnnotation() {
        // given
        var expected = Builder.class.getName();

        // when
        var result = sut.getSupportedAnnotationTypes();

        // then
        assertThat(result).containsOnly(expected);
    }

    @Test
    void shouldSupportLatestVersion() {
        // given
        var expected = SourceVersion.latest();

        // when
        var result = sut.getSupportedSourceVersion();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldSupportExpectedOptions() {
        // given
        var expected =
                BuilderOption.all().stream().map(BuilderOption::compilerName).collect(toUnmodifiableSet());

        // when
        var result = sut.getSupportedOptions();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldPrintOptionArgumentsErrors() {
        var errorMessage1 = PODAM.manufacturePojo(String.class);
        var errorMessage2 = PODAM.manufacturePojo(String.class);

        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);

            var error1 = processingException(element, errorMessage1);
            var error2 = processingException(element, errorMessage2);
            var options = nullsToEmptyString(processingEnv.getOptions());

            given(optionCompilerArgumentsValidator.validate(options)).willReturn(List.of(error1, error2));

            // when
            var result = sut.process(Set.of(annotation), roundEnvironment);

            // then
            assertThat(result).isTrue();

            verifyNoMoreInteractions(optionCompilerArgumentsValidator);
            verifyNoMoreInteractions(annotation, roundEnvironment);
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .executeTest();
    }

    @Test
    void shouldNotValidateOptionArgumentsOnSubsequentRounds() {
        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);

            given(optionCompilerArgumentsValidator.validate(Map.of())).willReturn(List.of());

            // when
            sut.process(Set.of(), roundEnvironment);
            var result = sut.process(Set.of(), roundEnvironment);

            // then
            assertThat(result).isTrue();

            verify(optionCompilerArgumentsValidator).validate(Map.of());
            verifyNoMoreInteractions(optionCompilerArgumentsValidator);
            verifyNoInteractions(roundEnvironment, annotation);
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .thenExpectThat()
                .compilationSucceeds()
                .executeTest();
    }

    @Test
    void shouldPrintSingleError() {
        var errorMessage = PODAM.manufacturePojo(String.class);

        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);

            var error = processingException(element, errorMessage);

            given(optionCompilerArgumentsValidator.validate(Map.of())).willReturn(Set.of());
            willReturn(Set.of(element)).given(roundEnvironment).getElementsAnnotatedWith(annotation);

            given(singleElementFileGenerator.generate(element)).willThrow(error);

            will(invocation -> {
                        processingEnv.getMessager().printError(errorMessage);
                        return null;
                    })
                    .given(processingExceptionPrinter)
                    .print(error);

            // when
            var result = sut.process(Set.of(annotation), roundEnvironment);

            // then
            assertThat(result).isTrue();

            var inOrder = inOrder(
                    roundEnvironment,
                    optionCompilerArgumentsValidator,
                    singleElementFileGenerator,
                    processingExceptionPrinter);
            inOrder.verify(optionCompilerArgumentsValidator).validate(Map.of());
            inOrder.verify(roundEnvironment).getElementsAnnotatedWith(annotation);
            inOrder.verify(singleElementFileGenerator).generate(element);
            inOrder.verify(processingExceptionPrinter).print(error);
            inOrder.verifyNoMoreInteractions();

            verifyNoInteractions(annotation);
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .contains(errorMessage)
                .executeTest();
    }

    @Test
    void shouldPrintAggregatedErrors() {
        var firstErrorMessage = PODAM.manufacturePojo(String.class);
        var secondErrorMessage = PODAM.manufacturePojo(String.class);

        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);

            var aggregatedError = processingExceptions(List.of(
                    processingException(element, firstErrorMessage), processingException(element, secondErrorMessage)));

            given(optionCompilerArgumentsValidator.validate(Map.of())).willReturn(Set.of());
            willReturn(Set.of(element)).given(roundEnvironment).getElementsAnnotatedWith(annotation);

            given(singleElementFileGenerator.generate(element)).willThrow(aggregatedError);

            will(invocation -> {
                        processingEnv.getMessager().printError(firstErrorMessage, element);
                        processingEnv.getMessager().printError(secondErrorMessage, element);
                        return null;
                    })
                    .given(processingExceptionPrinter)
                    .print(aggregatedError);

            // when
            var result = sut.process(Set.of(annotation), roundEnvironment);

            // then
            assertThat(result).isTrue();

            var inOrder = inOrder(
                    roundEnvironment,
                    optionCompilerArgumentsValidator,
                    singleElementFileGenerator,
                    processingExceptionPrinter);
            inOrder.verify(optionCompilerArgumentsValidator).validate(Map.of());
            inOrder.verify(roundEnvironment).getElementsAnnotatedWith(annotation);
            inOrder.verify(singleElementFileGenerator).generate(element);
            inOrder.verify(processingExceptionPrinter).print(aggregatedError);
            inOrder.verifyNoMoreInteractions();

            verifyNoInteractions(annotation);
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .contains(firstErrorMessage)
                .andThat()
                .compilerMessage()
                .ofKindError()
                .contains(secondErrorMessage)
                .executeTest();
    }

    @Captor
    private ArgumentCaptor<ProcessingException> processingExceptionCaptor;

    @Test
    void shouldPrintIOError() {
        var message = "BOOM!";
        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);
            var javaFile = new GeneratedJavaFile("wrong.package.name.SomeClass", "can't compile this");

            given(optionCompilerArgumentsValidator.validate(Map.of())).willReturn(Set.of());
            willReturn(Set.of(element)).given(roundEnvironment).getElementsAnnotatedWith(annotation);

            given(singleElementFileGenerator.generate(element)).willReturn(javaFile);
            willThrow(new UncheckedIOException(new IOException(message)))
                    .given(generatedJavaFileWriter)
                    .write(javaFile);

            will(invocation -> {
                        processingEnv.getMessager().printError(message);
                        return null;
                    })
                    .given(processingExceptionPrinter)
                    .print(any(ProcessingException.class));

            // when
            var result = sut.process(Set.of(annotation), roundEnvironment);

            // then
            assertThat(result).isTrue();

            var inOrder = inOrder(
                    roundEnvironment,
                    optionCompilerArgumentsValidator,
                    singleElementFileGenerator,
                    processingExceptionPrinter,
                    generatedJavaFileWriter);
            inOrder.verify(optionCompilerArgumentsValidator).validate(Map.of());
            inOrder.verify(roundEnvironment).getElementsAnnotatedWith(annotation);
            inOrder.verify(singleElementFileGenerator).generate(element);
            inOrder.verify(generatedJavaFileWriter).write(javaFile);
            inOrder.verify(processingExceptionPrinter).print(processingExceptionCaptor.capture());
            inOrder.verifyNoMoreInteractions();

            verifyNoInteractions(annotation);

            assertThat(processingExceptionCaptor.getValue().getMessage())
                    .startsWith("I/O error occurred while writing builder source:");
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage()
                .ofKindError()
                .equals(message)
                .executeTest();
    }

    @Test
    void shouldSucceed() {
        UnitTest<Element> unitTest = (processingEnv, element) -> {
            // given
            sut.init(processingEnv);
            var javaFile = new GeneratedJavaFile(
                    "org.example.SomeExample",
                    """
                package org.example;
                public class SomeExample {}
                """);

            given(optionCompilerArgumentsValidator.validate(Map.of())).willReturn(Set.of());
            willReturn(Set.of(element)).given(roundEnvironment).getElementsAnnotatedWith(annotation);

            given(singleElementFileGenerator.generate(element)).willReturn(javaFile);

            // when
            var result = sut.process(Set.of(annotation), roundEnvironment);

            // then
            assertThat(result).isTrue();

            var inOrder = inOrder(roundEnvironment, optionCompilerArgumentsValidator, singleElementFileGenerator);
            inOrder.verify(optionCompilerArgumentsValidator).validate(Map.of());
            inOrder.verify(roundEnvironment).getElementsAnnotatedWith(annotation);
            inOrder.verify(singleElementFileGenerator).generate(element);
            inOrder.verifyNoMoreInteractions();

            verifyNoInteractions(annotation, processingExceptionPrinter);
        };

        unitTest()
                .when()
                .passInElement()
                .fromClass(PassedIn.class)
                .intoUnitTest(unitTest)
                .thenExpectThat()
                .compilationSucceeds()
                .executeTest();
    }

    @PassIn
    private static class PassedIn {}

    private Map<String, String> nullsToEmptyString(Map<String, String> options) {
        return options.entrySet().stream()
                .collect(toUnmodifiableMap(Entry::getKey, e -> requireNonNullElse(e.getValue(), "")));
    }
}
