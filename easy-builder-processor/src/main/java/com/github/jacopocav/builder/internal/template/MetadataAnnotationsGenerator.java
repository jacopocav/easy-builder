package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.option.BuilderOption;
import com.github.jacopocav.builder.internal.option.Options;

import java.util.List;

public class MetadataAnnotationsGenerator {
    /**
     * @return attribute values for all the {@link Metadata} annotations that must be added
     * to the builder (one for every option in {@link BuilderOption})
     */
    public List<MetadataAnnotation> generate(Options options) {
        return BuilderOption.all().stream()
                .map(option -> makeMetadataAnnotation(option, options))
                .toList();
    }

    private MetadataAnnotation makeMetadataAnnotation(BuilderOption builderOption, Options options) {
        var value =
                switch (builderOption) {
                    case CLASS_NAME -> options.className().orElse("");
                    case SETTER_PREFIX -> options.setterPrefix();
                    case BUILD_METHOD_NAME -> options.buildMethodName();
                    case STATIC_FACTORY_NAME -> options.staticFactoryName();
                    case COPY_FACTORY_METHOD -> Boolean.toString(options.generateStaticFromMethod());
                    case COPY_FACTORY_METHOD_NAME -> options.staticFromMethodName();
                };

        return new MetadataAnnotation(builderOption.annotationName(), value);
    }
}
