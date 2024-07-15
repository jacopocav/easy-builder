package com.github.jacopocav.builder.internal.finder.strategy;

import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Found;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import static com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error.*;
import static com.github.jacopocav.builder.internal.util.MultiReleaseUtils.getFirst;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.util.ElementFilter.constructorsIn;

class ClassConstructorFinderStrategy implements CreatorMethodFinderStrategy {
    /**
     * @return {@link Found} containing the only appropriate constructor (i.e. non-private, with at least 1 argument) for the class
     * annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder};
     * <p>{@link CreatorMethod.Error#NOT_APPLICABLE}
     * if the annotated {@code element} is not a class,
     * {@link CreatorMethod.Error#NOT_FOUND} if
     * no valid constructor was found,
     * or {@link CreatorMethod.Error#TOO_MANY_FOUND}
     * if more than one valid constructor was found.
     */
    @Override
    public CreatorMethod find(Element annotatedElement) {
        if (annotatedElement.getKind() != CLASS) {
            return NOT_APPLICABLE;
        }

        var candidateConstructors = constructorsIn(annotatedElement.getEnclosedElements()).stream()
                .filter(this::hasAtLeastOneArg)
                .filter(this::isNotPrivate)
                .toList();

        return switch (candidateConstructors.size()) {
            case 0 -> NOT_FOUND;
            case 1 -> new Found(getFirst(candidateConstructors));
            default -> TOO_MANY_FOUND;
        };
    }

    private boolean hasAtLeastOneArg(ExecutableElement ctor) {
        return !ctor.getParameters().isEmpty();
    }

    private boolean isNotPrivate(ExecutableElement ctor) {
        return !ctor.getModifiers().contains(PRIVATE);
    }
}
