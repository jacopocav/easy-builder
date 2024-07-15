package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.finder.Accessor.Found;
import com.github.jacopocav.builder.internal.finder.Accessor.NotFound;
import com.github.jacopocav.builder.internal.finder.AccessorFinder;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.option.InterpolatedOptions;
import com.github.jacopocav.builder.internal.option.Options;
import com.github.jacopocav.builder.internal.option.RawOptions;
import com.github.jacopocav.builder.internal.util.StringUtils;
import com.github.jacopocav.builder.processing.error.ProcessingException;
import com.github.jacopocav.builder.processing.type.TypeRegistry;

import javax.lang.model.element.VariableElement;
import java.util.List;

import static com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration.*;
import static com.github.jacopocav.builder.processing.error.AggregatedProcessingException.processingExceptions;
import static com.github.jacopocav.builder.processing.error.ProcessingException.processingException;
import static java.util.stream.Collectors.partitioningBy;
import static javax.lang.model.element.ElementKind.METHOD;

public class MembersGenerator {
    private final AccessorFinder accessorFinder;

    public MembersGenerator(AccessorFinder accessorFinder) {
        this.accessorFinder = accessorFinder;
    }

    /**
     * Maps the parameters of {@code creatorMethod} to a list of {@link Member} objects that
     * will be used in the builder template to generate multiple parts of the builder class
     * (i.e. the private fields, the setters, etc.).
     *
     * @return list of all template context objects related to the parameters in {@code creatorMethod}
     */
    public List<Member> apply(BuilderData builderData, TypeRegistry typeRegistry) {
        var partitionedResults = builderData.creatorMethod().getParameters().stream()
                .map(param -> toMember(param, typeRegistry, builderData.options()))
                .collect(partitioningBy(MemberResult.Success.class::isInstance));
        var successes = partitionedResults.get(true);
        var failures = partitionedResults.get(false);

        requireEmpty(failures);

        return successes.stream().map(MemberResult.Success.class::cast).map(MemberResult.Success::member).toList();
    }

    private MemberResult toMember(VariableElement parameter, TypeRegistry typeRegistry, InterpolatedOptions options) {
        var name = parameter.getSimpleName().toString();
        var memberBuilder = Member.builder()
                .withType(typeRegistry.getUsageName(parameter.asType()))
                .withName(name)
                .withSetterName(StringUtils.composeSetterName(options.setterPrefix(), name));

        var copyFactoryMethodGeneration = options.copyFactoryMethod();

        if (copyFactoryMethodGeneration == DISABLED) {
            return new MemberResult.Success(memberBuilder.build());
        }

        var accessorResult = accessorFinder.apply(parameter);

        if (accessorResult instanceof NotFound) {
            return copyFactoryMethodGeneration == ENABLED
                    ? new MemberResult.Failure(
                            parameter,
                            processingException(
                                    parameter,
                                    "could not find any accessor (getter or field) for parameter %s. "
                                            + "Add it or disable static copy method generation with %s=%s or %s=%s",
                                    parameter.getSimpleName(),
                                    BuilderOption.COPY_FACTORY_METHOD.annotationName(), DISABLED,
                                    BuilderOption.COPY_FACTORY_METHOD.annotationName(), DYNAMIC))
                    : new MemberResult.Success(memberBuilder.build());
        }

        var accessor = ((Found) accessorResult).accessor();
        var suffix = accessor.getKind() == METHOD ? "()" : "";

        memberBuilder.withGetterName(accessor.getSimpleName().toString() + suffix);

        return new MemberResult.Success(memberBuilder.build());
    }

    private static void requireEmpty(List<MemberResult> failures) {
        if (!failures.isEmpty()) {
            var exceptions = failures.stream()
                    .map(MemberResult.Failure.class::cast)
                    .map(MemberResult.Failure::exception)
                    .toList();

            throw processingExceptions(exceptions);
        }
    }

    private sealed interface MemberResult {
        record Failure(VariableElement parameter, ProcessingException exception) implements MemberResult {}
        record Success(Member member) implements MemberResult {}
    }
}
