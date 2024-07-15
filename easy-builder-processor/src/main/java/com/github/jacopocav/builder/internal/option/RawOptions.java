package com.github.jacopocav.builder.internal.option;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;

/**
 * A record containing all the computed {@link BuilderOption} values for a specific
 * {@link com.github.jacopocav.builder.annotation.Builder @Builder}.
 */
public record RawOptions(
        String className,
        String setterPrefix,
        String buildMethodName,
        String staticFactoryName,
        CopyFactoryMethodGeneration copyFactoryMethod,
        String copyFactoryMethodName) implements Options {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String className;
        private String setterPrefix;
        private String buildMethodName;
        private String staticFactoryName;
        private CopyFactoryMethodGeneration copyFactoryMethod;
        private String copyFactoryMethodName;

        private Builder() {}

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder setterPrefix(String setterPrefix) {
            this.setterPrefix = setterPrefix;
            return this;
        }

        public Builder buildMethodName(String buildMethodName) {
            this.buildMethodName = buildMethodName;
            return this;
        }

        public Builder staticFactoryName(String staticFactoryName) {
            this.staticFactoryName = staticFactoryName;
            return this;
        }

        public Builder copyFactoryMethod(CopyFactoryMethodGeneration copyFactoryMethod) {
            this.copyFactoryMethod = copyFactoryMethod;
            return this;
        }

        public Builder copyFactoryMethodName(String copyFactoryMethodName) {
            this.copyFactoryMethodName = copyFactoryMethodName;
            return this;
        }

        public RawOptions build() {
            return new RawOptions(
                    className,
                    setterPrefix,
                    buildMethodName,
                    staticFactoryName,
                    copyFactoryMethod,
                    copyFactoryMethodName);
        }
    }
}
