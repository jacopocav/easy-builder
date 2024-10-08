package com.github.jacopocav.builder.internal.option;

import static com.github.jacopocav.builder.internal.error.ProcessingException.processingException;

import com.github.jacopocav.builder.internal.error.ProcessingException;
import com.github.jacopocav.builder.internal.validation.JavaNameValidator;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class OptionCompilerArgumentsValidator {
    private static final String ERROR_MESSAGE_TEMPLATE =
            "value \"%s\" of compiler argument %s is not a permitted Java identifier";
    private static final String PREFIX_ERROR_MESSAGE_TEMPLATE = ERROR_MESSAGE_TEMPLATE + " prefix";
    private final JavaNameValidator javaNameValidator;

    public OptionCompilerArgumentsValidator(JavaNameValidator javaNameValidator) {
        this.javaNameValidator = javaNameValidator;
    }

    /**
     * Validates the options received by the compiler.
     *
     * @return all the errors encountered during validation, or an empty collection if the options are valid.
     */
    public Collection<ProcessingException> validate(Map<String, String> options) {
        return options.entrySet().stream()
                .map(this::validateArgument)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ProcessingException> validateArgument(Entry<String, String> entry) {
        var name = entry.getKey();
        var value = entry.getValue();

        var option = BuilderOption.findByCompilerName(name);

        return option.filter(opt -> isInvalidNamePrefix(opt, value))
                .map(opt ->
                        processingException(null, PREFIX_ERROR_MESSAGE_TEMPLATE.formatted(value, opt.compilerName())))
                .or(() -> option.filter(opt -> isInvalidName(opt, value))
                        .map(opt1 -> processingException(null, ERROR_MESSAGE_TEMPLATE.formatted(value, name))));
    }

    private boolean isInvalidNamePrefix(BuilderOption option, String value) {
        return option.isPrefix() && !javaNameValidator.isValidNamePrefix(value);
    }

    private boolean isInvalidName(BuilderOption option, String value) {
        return !option.isPrefix() && !javaNameValidator.isValidName(value);
    }
}
