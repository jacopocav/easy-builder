import com.github.jacopocav.builder.processor.BuilderProcessor;
import javax.annotation.processing.Processor;

module com.github.jacopocav.easybuilder.processor {
    requires com.github.jacopocav.easybuilder;
    requires java.compiler;
    requires gg.jte.runtime;
    requires gg.jte.models;

    provides Processor with
            BuilderProcessor;
}
