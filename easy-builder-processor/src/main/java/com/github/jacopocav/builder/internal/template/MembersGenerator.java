package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.finder.Accessor.Found;
import com.github.jacopocav.builder.internal.finder.Accessor.NotFound;
import com.github.jacopocav.builder.internal.finder.AccessorFinder;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.option.Options;
import com.github.jacopocav.builder.internal.util.StringUtils;
import com.github.jacopocav.builder.processing.error.ProcessingException;
import com.github.jacopocav.builder.processing.type.TypeRegistry;

import javax.lang.model.element.VariableElement;
import java.util.List;

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
                .collect(partitioningBy(Success.class::isInstance));
        var successes = partitionedResults.get(true);
        var failures = partitionedResults.get(false);

        requireEmpty(failures);

        return successes.stream().map(Success.class::cast).map(Success::member).toList();
    }

    private MemberResult toMember(VariableElement parameter, TypeRegistry typeRegistry, Options options) {
        var name = parameter.getSimpleName().toString();
        var memberBuilder = Member.builder()
                .withType(typeRegistry.getUsageName(parameter.asType()))
                .withName(name)
                .withSetterName(StringUtils.composeSetterName(options.setterPrefix(), name));

        if (options.generateStaticFromMethod()) {
            var accessorResult = accessorFinder.apply(parameter);

            if (accessorResult instanceof NotFound) {
                return new Failure(
                        parameter,
                        processingException(
                                parameter,
                                "could not find any accessor (getter or field) for parameter %s. "
                                        + "Add it or disable static copy method generation with %s=false",
                                parameter.getSimpleName(),
                                BuilderOption.COPY_FACTORY_METHOD.annotationName()));
            }

            var accessor = ((Found) accessorResult).accessor();
            var suffix = accessor.getKind() == METHOD ? "()" : "";

            memberBuilder.withGetterName(accessor.getSimpleName().toString() + suffix);
        }

        return new Success(memberBuilder.build());
    }

    private static void requireEmpty(List<MemberResult> failures) {
        if (!failures.isEmpty()) {
            var exceptions = failures.stream()
                    .map(Failure.class::cast)
                    .map(Failure::exception)
                    .toList();

            throw processingExceptions(exceptions);
        }
    }

    private sealed interface MemberResult permits Success, Failure {}

    private record Failure(VariableElement parameter, ProcessingException exception) implements MemberResult {}

    private record Success(Member member) implements MemberResult {}
}
