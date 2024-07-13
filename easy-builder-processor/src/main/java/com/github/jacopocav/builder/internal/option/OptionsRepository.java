package com.github.jacopocav.builder.internal.option;

import javax.lang.model.element.Element;

public interface OptionsRepository {
    /**
     * @return the computed {@link Options} for the given {@code element}.
     * Option values are determined according to this hierarchy:
     * <ol>
     *     <li>Options explicitly specified on
     *     {@link com.github.jacopocav.builder.annotation.Builder @Builder} attributes
     *     <li>Options passed as compiler arguments
     *     <li>Default values specified in {@link com.github.jacopocav.builder.annotation.BuilderDefaults}
     * </ol>
     */
    Options get(Element element);
}
