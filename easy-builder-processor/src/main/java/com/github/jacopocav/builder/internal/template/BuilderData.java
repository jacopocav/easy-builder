package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.internal.option.InterpolatedOptions;
import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeName;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public record BuilderData(
        GeneratedTypeName name,
        InterpolatedOptions options,
        ExecutableElement creatorMethod,
        TypeElement targetClass,
        TypeElement enclosingClass) {}
