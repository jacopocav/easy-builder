package com.github.jacopocav.builder.internal.template;

/**
 * Variables related to a single builder "member" (i.e. property)
 */
public record Member(String type, String name, String setterName, String getterName) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String type;
        private String name;
        private String setterName;
        private String getterName;

        private Builder() {}

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSetterName(String setterName) {
            this.setterName = setterName;
            return this;
        }

        public Builder withGetterName(String getterName) {
            this.getterName = getterName;
            return this;
        }

        public Member build() {
            return new Member(type, name, setterName, getterName);
        }
    }
}
