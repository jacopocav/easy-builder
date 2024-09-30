package com.github.jacopocav.builder.internal.util;

import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class AnnotationMirrorRetrieverImpl implements AnnotationMirrorRetriever {
    @Override
    @SuppressWarnings("unchecked")
    public Optional<AnnotationMirror> findByType(Element annotatedElement, TypeMirror annotationType) {
        return (Optional<AnnotationMirror>) annotatedElement.getAnnotationMirrors().stream()
                .filter(mirror -> mirror.getAnnotationType().equals(annotationType))
                .findFirst();
    }
}
