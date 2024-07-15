package com.github.jacopocav.builder.internal.option;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public interface OptionsRepository {
    /**
     * @return the computed {@link RawOptions} for the given {@code element}.
     * Option values are determined according to this hierarchy:
     * <ol>
     *     <li>Options explicitly specified on
     *     {@link com.github.jacopocav.builder.annotation.Builder @Builder} attributes
     *     <li>Options passed as compiler arguments
     *     <li>Default values specified in {@link com.github.jacopocav.builder.annotation.BuilderDefaults}
     * </ol>
     */
    RawOptions getRaw(Element annotatedElement);

    InterpolatedOptions getInterpolated(RawOptions raw, TypeElement enclosingType);
}
