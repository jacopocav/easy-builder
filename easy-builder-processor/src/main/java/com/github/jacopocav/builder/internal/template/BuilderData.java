package com.github.jacopocav.builder.internal.template;

import com.github.jacopocav.builder.processing.generation.name.GeneratedTypeName;
import com.github.jacopocav.builder.internal.option.Options;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public record BuilderData(
        GeneratedTypeName name,
        Options options,
        ExecutableElement creatorMethod,
        TypeElement sourceClass,
        TypeElement enclosingClass) {}
