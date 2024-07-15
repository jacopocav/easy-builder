package com.github.jacopocav.builder.processing.util;

import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public interface AnnotationMirrorRetriever {

    Optional<AnnotationMirror> findByType(Element annotatedElement, TypeMirror annotationType);
}
