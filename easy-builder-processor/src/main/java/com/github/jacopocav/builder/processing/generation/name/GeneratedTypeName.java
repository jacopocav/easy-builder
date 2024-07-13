package com.github.jacopocav.builder.processing.generation.name;

import javax.lang.model.element.PackageElement;

/**
 * The name of a generated java class
 * @param enclosingPackage
 * @param simpleName
 */
public record GeneratedTypeName(PackageElement enclosingPackage, String simpleName) {
    public String qualifiedName() {
        return enclosingPackage.getQualifiedName().toString() + "." + simpleName;
    }
}
