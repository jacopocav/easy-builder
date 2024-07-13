package com.github.jacopocav.builder.internal;

import javax.lang.model.element.PackageElement;

public record BuilderName(PackageElement enclosingPackage, String simpleName) {}
