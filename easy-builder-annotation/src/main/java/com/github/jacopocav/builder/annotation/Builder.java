package com.github.jacopocav.builder.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@Target({TYPE, CONSTRUCTOR, METHOD})
public @interface Builder {
    /**
     * Placeholder that will be replaced with the name of the annotated class.
     */
    String SOURCE_CLASS_NAME = "{SourceClassName}";
    /**
     * Placeholder that will be replaced with the name of the annotated class, with a lower-case initial.
     */
    String LOWER_CASE_SOURCE_CLASS_NAME = "{lowerCaseSourceClassName}";

    /**
     * Name of the generated builder class.
     * Defaults to the {@value Defaults#CLASS_NAME}.
     * <p>
     * Note that the builder will always be generated in the same package as its
     */
    String className() default Defaults.CLASS_NAME;

    /**
     * Name of the factory method that will return a new builder instance.
     * Defaults to {@value Defaults#FACTORY_METHOD_NAME}.
     */
    String factoryMethodName() default Defaults.FACTORY_METHOD_NAME;

    /**
     * Prefix that will be used on builder setters (can be empty).
     * Defaults to {@value Defaults#SETTER_PREFIX}.
     */
    String setterPrefix() default Defaults.SETTER_PREFIX;

    /**
     * Name of the method that builds an instance of the source class from the builder.
     * Defaults to {@value Defaults#BUILD_METHOD_NAME}.
     */
    String buildMethodName() default Defaults.BUILD_METHOD_NAME;

    /**
     * Generates a static factory method that creates a new instance of the builder by copying all properties a given
     * source class instance given as argument.
     * <p>
     * The generation of this method is optional, as it requires that all source class properties to be <em>accessible</em>
     * from the builder. A property is accessible if its getter or field can be read from the builder class without
     * using reflection.
     * <p>
     * Defaults to {@link CopyFactoryMethodGeneration#ENABLED_STRICT}.
     */
    CopyFactoryMethodGeneration copyFactoryMethod() default CopyFactoryMethodGeneration.ENABLED_STRICT;

    /**
     * Name of the static copy factory method, if enabled with {@link #copyFactoryMethod()}.
     * Defaults to {@value Defaults#COPY_FACTORY_METHOD_NAME}.
     */
    String copyFactoryMethodName() default Defaults.COPY_FACTORY_METHOD_NAME;

    enum CopyFactoryMethodGeneration {
        /**
         * A copy factory method will not be generated
         */
        DISABLED,
        /**
         * A copy factory method will be generated, and a compilation will fail if some property of the
         * source class is not accessible (i.e. has no accessible getter or field).
         */
        ENABLED_STRICT,
        /**
         * A copy factory method will be generated only if all properties of the source class are accessible
         * (i.e. its getter or field are accessible without using reflection).
         * <p>A compiler warning will be printed in case one or more properties are not accessible.
         */
        ENABLED_LENIENT
    }

    class Defaults {
        private Defaults() {}

        public static final String CLASS_NAME = SOURCE_CLASS_NAME + "Builder";
        public static final String FACTORY_METHOD_NAME = "create";
        public static final String SETTER_PREFIX = "";
        public static final String BUILD_METHOD_NAME = "build";
        public static final CopyFactoryMethodGeneration COPY_FACTORY_METHOD =
                CopyFactoryMethodGeneration.ENABLED_STRICT;
        public static final String COPY_FACTORY_METHOD_NAME = "from";
    }
}
