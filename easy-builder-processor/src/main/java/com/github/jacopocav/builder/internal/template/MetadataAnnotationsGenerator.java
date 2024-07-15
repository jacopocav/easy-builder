package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.annotation.Builder.CopyFactoryMethodGeneration;
import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.option.RawOptions;

import java.util.List;

public class MetadataAnnotationsGenerator {
    /**
     * @return attribute values for all the {@link Metadata} annotations that must be added
     * to the builder (one for every option in {@link BuilderOption})
     */
    public List<MetadataAnnotation> generate(RawOptions options) {
        return BuilderOption.all().stream()
                .map(option -> makeMetadataAnnotation(option, options))
                .toList();
    }

    private MetadataAnnotation makeMetadataAnnotation(BuilderOption builderOption, RawOptions options) {
        var value =
                switch (builderOption) {
                    case CLASS_NAME -> quote(options.className());
                    case SETTER_PREFIX -> quote(options.setterPrefix());
                    case BUILD_METHOD_NAME -> quote(options.buildMethodName());
                    case FACTORY_METHOD_NAME -> quote(options.staticFactoryName());
                    case COPY_FACTORY_METHOD -> CopyFactoryMethodGeneration.class.getSimpleName() + "."
                            + options.copyFactoryMethod().name();
                    case COPY_FACTORY_METHOD_NAME -> quote(options.copyFactoryMethodName());
                };

        return new MetadataAnnotation(builderOption.annotationName(), value);
    }

    private static String quote(String value) {
        return "\"" + value + "\"";
    }
}
