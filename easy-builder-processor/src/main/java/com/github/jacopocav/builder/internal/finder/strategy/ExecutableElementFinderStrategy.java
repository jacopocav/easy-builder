package com.github.jacopocav.builder.internal.finder.strategy;

import com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Found;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import static com.github.jacopocav.builder.internal.finder.strategy.CreatorMethod.Error.NOT_APPLICABLE;

class ExecutableElementFinderStrategy implements CreatorMethodFinderStrategy {
    /**
     * @return {@link Found} with the {@code element} annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder}
     * if it's a method or a constructor, {@link CreatorMethod.Error#NOT_APPLICABLE} otherwise
     */
    @Override
    public CreatorMethod find(Element element) {
        return switch (element.getKind()) {
            case METHOD, CONSTRUCTOR -> new Found((ExecutableElement) element);
            default -> NOT_APPLICABLE;
        };
    }
}
