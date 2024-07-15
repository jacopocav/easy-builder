package com.github.jacopocav.builder.internal.finder.strategy;

import static javax.lang.model.element.ElementKind.RECORD;
import static javax.lang.model.util.ElementFilter.constructorsIn;

import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error;
import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Found;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

class RecordConstructorFinderStrategy implements CreatorMethodFinderStrategy {
    /**
     * Finds the canonical constructor for the record annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder}.
     * @return {@link Found} with the canonical constructor;
     * <p>{@link Error#NOT_APPLICABLE}
     * if {@code element} is not a record,
     * {@link Error#NOT_FOUND} if
     * the record has zero components.
     */
    @Override
    public CreatorMethod find(Element element) {
        if (element.getKind() != RECORD) {
            return Error.NOT_APPLICABLE;
        }

        var recordElement = (TypeElement) element;

        if (recordElement.getRecordComponents().isEmpty()) {
            return Error.NOT_FOUND;
        }

        return constructorsIn(recordElement.getEnclosedElements()).stream()
                .filter(constructor -> parametersMatchRecordComponents(recordElement, constructor))
                .findFirst()
                .<CreatorMethod>map(CreatorMethod.Found::new)
                .orElse(Error.NOT_FOUND);
    }

    private boolean parametersMatchRecordComponents(TypeElement recordElement, ExecutableElement constructor) {
        var componentTypes = recordElement.getRecordComponents().stream()
                .map(c -> c.asType().toString())
                .toList();

        var parameterTypes = constructor.getParameters().stream()
                .map(p -> p.asType().toString())
                .toList();

        return componentTypes.equals(parameterTypes);
    }
}
