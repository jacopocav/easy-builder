package com.github.jacopocav.builder.internal.option;

import java.util.Optional;

/**
 * A record containing all the computed {@link BuilderOption} values for a specific
 * {@link com.github.jacopocav.builder.annotation.Builder @Builder}.
 */
public record Options(
        Optional<String> className,
        String setterPrefix,
        String buildMethodName,
        String staticFactoryName,
        boolean generateStaticFromMethod,
        String staticFromMethodName) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * We can't use {@link com.github.jacopocav.builder.annotation.Builder @Builder} here :D
     */
    public static final class Builder {
        private Optional<String> className = Optional.empty();
        private String setterPrefix;
        private String buildMethodName;
        private String staticFactoryName;
        private boolean generateStaticFromMethod;
        private String staticFromMethodName;

        private Builder() {}

        public Builder withClassName(Optional<String> className) {
            this.className = className;
            return this;
        }

        public Builder withSetterPrefix(String setterPrefix) {
            this.setterPrefix = setterPrefix;
            return this;
        }

        public Builder withBuildMethodName(String buildMethodName) {
            this.buildMethodName = buildMethodName;
            return this;
        }

        public Builder withStaticFactoryName(String staticFactoryName) {
            this.staticFactoryName = staticFactoryName;
            return this;
        }

        public Builder withGenerateStaticFromMethod(boolean generateStaticFromMethod) {
            this.generateStaticFromMethod = generateStaticFromMethod;
            return this;
        }

        public Builder withStaticFromMethodName(String staticFromMethodName) {
            this.staticFromMethodName = staticFromMethodName;
            return this;
        }

        public Options build() {
            return new Options(
                    className,
                    setterPrefix,
                    buildMethodName,
                    staticFactoryName,
                    generateStaticFromMethod,
                    staticFromMethodName);
        }
    }
}
