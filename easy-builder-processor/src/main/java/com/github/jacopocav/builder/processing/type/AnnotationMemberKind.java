package com.github.jacopocav.builder.processing.type;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.ElementKind.ENUM;

/**
 * Contains all kinds of values an annotation member can have (primitives, class literals, enum constants, strings,
 * other annotations, and arrays containing any of the previous types)
 */
public enum AnnotationMemberKind {
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE,
    CLASS_LITERAL,
    ENUM_CONSTANT,
    STRING,
    ANNOTATION,
    ARRAY;

    /**
     * @return {@code true} if this represents a primitive type, {@code false} otherwise
     */
    public boolean isPrimitive() {
        return switch (this) {
            case BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE -> true;
            case CLASS_LITERAL, ENUM_CONSTANT, STRING, ANNOTATION, ARRAY -> false;
        };
    }

    /**
     * Returns the {@link AnnotationMemberKind} of {@code typeMirror}
     *
     * @throws IllegalArgumentException if {@code typeMirror} is a type that cannot be used for an annotation member
     */
    public static AnnotationMemberKind of(TypeMirror typeMirror) {
        var kind = typeMirror.getKind();
        return switch (kind) {
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case CHAR -> CHAR;
            case SHORT -> SHORT;
            case INT -> INT;
            case LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            case ARRAY -> ARRAY;
            case DECLARED -> ofDeclared((DeclaredType) typeMirror);
            default -> throw illegalType(typeMirror);
        };
    }

    private static AnnotationMemberKind ofDeclared(DeclaredType declaredType) {
        var typeElement = (TypeElement) declaredType.asElement();

        var qualifiedName = typeElement.getQualifiedName();
        if (qualifiedName.contentEquals(String.class.getName())) {
            return STRING;
        }
        if (qualifiedName.contentEquals(Class.class.getName())) {
            return CLASS_LITERAL;
        }

        var elementKind = typeElement.getKind();
        if (elementKind == ENUM) {
            return ENUM_CONSTANT;
        }
        if (elementKind == ANNOTATION_TYPE) {
            return ANNOTATION;
        }

        throw illegalType(declaredType);
    }

    private static IllegalArgumentException illegalType(TypeMirror typeMirror) {
        return new IllegalArgumentException("illegal annotation member type: " + typeMirror);
    }
}
