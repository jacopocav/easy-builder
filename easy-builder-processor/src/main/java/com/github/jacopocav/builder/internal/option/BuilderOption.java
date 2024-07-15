package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder;
import com.github.jacopocav.builder.annotation.Builder.Defaults;
import com.github.jacopocav.builder.processor.BuilderProcessor;
import java.util.Optional;
import java.util.Set;

/**
 * Options supported by {@link BuilderProcessor}, both as
 * compiler arguments and as {@link Builder} annotation
 * attributes.
 */
public enum BuilderOption {
    CLASS_NAME("className", Defaults.CLASS_NAME, false),
    SETTER_PREFIX("setterPrefix", Defaults.SETTER_PREFIX, true),
    BUILD_METHOD_NAME("buildMethodName", Defaults.BUILD_METHOD_NAME, false),
    FACTORY_METHOD_NAME("factoryMethodName", Defaults.FACTORY_METHOD_NAME, false),
    COPY_FACTORY_METHOD("copyFactoryMethod", Defaults.COPY_FACTORY_METHOD, false),
    COPY_FACTORY_METHOD_NAME("copyFactoryMethodName", Defaults.COPY_FACTORY_METHOD_NAME, false);

    private static final Set<BuilderOption> ALL = Set.of(values());

    private final String annotationName;
    private final Object defaultValue;
    private final boolean isPrefix;

    BuilderOption(String annotationName, Object defaultValue, boolean isPrefix) {
        this.annotationName = annotationName;
        this.defaultValue = defaultValue;
        this.isPrefix = isPrefix;
    }

    public static Set<BuilderOption> all() {
        return ALL;
    }

    public static Optional<BuilderOption> findByCompilerName(String compilerName) {
        return all().stream()
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
        return "easy.builder." + annotationName();
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValue() {
        return (T) defaultValue;
    }

    /**
     * Indicates whether the value of the option is to be used as a prefix for a java method, or
     * as a full name.
     */
    public boolean isPrefix() {
        return isPrefix;
    }
}
