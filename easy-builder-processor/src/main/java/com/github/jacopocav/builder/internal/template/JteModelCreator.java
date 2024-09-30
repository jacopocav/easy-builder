package com.github.jacopocav.builder.internal.template;

import static javax.lang.model.element.ElementKind.METHOD;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;
import com.github.jacopocav.builder.annotation.GeneratedBuilder;
import com.github.jacopocav.builder.internal.template.jte.Templates;
import com.github.jacopocav.builder.internal.type.TypeRegistry;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import gg.jte.models.runtime.JteModel;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Creates a {@link JteModel} of the builder class, ready to be rendered with {@link JteModel#render()}
 */
public class JteModelCreator {
    private final Clock clock;
    private final MembersGenerator membersGenerator;
    private final GeneratedBuilderOptionsRetriever generatedBuilderOptionsRetriever;
    private final Supplier<TypeRegistry> typeRegistryFactory;
    private final Templates templates;

    public JteModelCreator(
            Clock clock,
            MembersGenerator membersGenerator,
            GeneratedBuilderOptionsRetriever generatedBuilderOptionsRetriever,
            Supplier<TypeRegistry> typeRegistryFactory,
            Templates templates) {
        this.membersGenerator = membersGenerator;
        this.clock = clock;
        this.generatedBuilderOptionsRetriever = generatedBuilderOptionsRetriever;
        this.typeRegistryFactory = typeRegistryFactory;
        this.templates = templates;
    }

    public JteModel create(BuilderData builderData) {
        var builderName = builderData.name();
        var creatorMethod = builderData.creatorMethod();
        var options = builderData.options();

        var typeRegistry = typeRegistryFactory.get();

        var packageName = builderName.enclosingPackage().getQualifiedName().toString();
        var processorName = BuilderProcessor.class.getName();
        var creationTimestamp = OffsetDateTime.now(clock);
        var targetClassName = typeRegistry.getUsageName(builderData.targetClass());
        var enclosingClassName = typeRegistry.getUsageName(builderData.enclosingClass());
        var generatedBuilderOptions = generatedBuilderOptionsRetriever.get(options.raw());
        var className = builderName.simpleName();
        var members = membersGenerator.apply(builderData, typeRegistry);
        var staticCreatorMethod = creatorMethod.getKind() == METHOD
                ? creatorMethod.getSimpleName().toString()
                : "";
        var generateCopyFactoryMethod =
                switch (options.copyFactoryMethod()) {
                    case DISABLED -> false;
                    case ENABLED -> true;
                    case DYNAMIC -> members.stream().map(Member::getterName).allMatch(Objects::nonNull);
                };

        typeRegistry.register(GeneratedBuilder.class);
        typeRegistry.register(CopyFactoryMethodGeneration.class);
        typeRegistry.register(javax.annotation.processing.Generated.class);

        return templates.builder(
                packageName,
                typeRegistry,
                processorName,
                creationTimestamp,
                generatedBuilderOptions,
                className,
                members,
                options,
                targetClassName,
                enclosingClassName,
                staticCreatorMethod,
                generateCopyFactoryMethod);
    }
}
