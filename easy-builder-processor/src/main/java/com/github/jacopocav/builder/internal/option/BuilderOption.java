package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.annotation.Builder.Defaults;
import com.github.jacopocav.builder.processor.BuilderProcessor;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Options supported by {@link BuilderProcessor}, both as
 * compiler arguments and as {@link Builder} annotation
 * attributes.
 */
public enum BuilderOption {
    CLASS_NAME("className", Defaults.CLASS_NAME, false, false),
    SETTER_PREFIX("setterPrefix", Defaults.SETTER_PREFIX, true, true),
    BUILD_METHOD_NAME("buildMethodName", Defaults.BUILD_METHOD_NAME, true, false),
    STATIC_FACTORY_NAME("staticFactoryName", Defaults.FACTORY_METHOD_NAME, true, false),
    COPY_FACTORY_METHOD("generateStaticFromMethod", Defaults.COPY_FACTORY_METHOD, true, false),
    COPY_FACTORY_METHOD_NAME("staticFromMethodName", Defaults.COPY_FACTORY_METHOD_NAME, true, false);

    private static final Set<BuilderOption> ALL = Set.of(values());
    private static final Set<BuilderOption> COMPILER_ARGUMENTS =
            ALL.stream().filter(BuilderOption::isCompilerArgument).collect(toUnmodifiableSet());

    private final String annotationName;
    private final Object defaultValue;
    private final boolean isCompilerArgument;
    private final boolean isPrefix;

    BuilderOption(String annotationName, Object defaultValue, boolean isCompilerArgument, boolean isPrefix) {
        this.annotationName = annotationName;
        this.defaultValue = defaultValue;
        this.isCompilerArgument = isCompilerArgument;
        this.isPrefix = isPrefix;
    }

    public static Set<BuilderOption> all() {
        return ALL;
    }

    public static Set<BuilderOption> allCompilerArguments() {
        return COMPILER_ARGUMENTS;
    }

    public static Optional<BuilderOption> findByCompilerName(String compilerName) {
        return allCompilerArguments().stream()
                .filter(opt -> opt.compilerName().equals(compilerName))
                .findFirst();
    }

    /**
     * The name of the {@link Builder @Builder} annotation
     * attribute that represents this option.
     */
    public String annotationName() {
        return annotationName;
    }

    /**
     * The name of the option when received as a compiler argument (as {@code -AcompilerName=value})
     */
    public String compilerName() {
        return "builder." + annotationName();
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValue() {
        return (T) defaultValue;
    }

    public boolean isCompilerArgument() {
        return isCompilerArgument;
    }

    /**
     * Indicates whether the value of the option is to be used as a prefix for a java method, or
     * as a full name.
     */
    public boolean isPrefix() {
        return isPrefix;
    }
}
