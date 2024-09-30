package com.github.jacopocav.builder.internal;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Determines the type of object that will be constructed by the builder, starting from
 * an {@code element} annotated with {@link com.github.jacopocav.builder.annotation.Builder @Builder}.
 */
public class SourceClassRetriever {
    private final Types types;

    SourceClassRetriever(Types types) {
        this.types = types;
    }

    public TypeMirror getMirror(Element element) {
        return switch (element.getKind()) {
            case CLASS, RECORD -> element.asType();
            case PARAMETER -> getMirror(element.getEnclosingElement());
            case METHOD -> ((ExecutableElement) element).getReturnType();
            case CONSTRUCTOR -> element.getEnclosingElement().asType();
            default -> throw new AssertionError("unexpected element kind: %s".formatted(element.getKind()));
        };
    }

    public TypeElement getElement(Element element) {
        return (TypeElement) types.asElement(getMirror(element));
    }
}
