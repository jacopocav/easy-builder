package com.github.jacopocav.builder.processing.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public interface AnnotationMirrorRetriever {

    Optional<AnnotationMirror> findByType(Element annotatedElement, TypeMirror annotationType);
}
