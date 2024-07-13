package com.github.jacopocav.builder.processing.generation.name;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public interface GeneratedTypeNameGenerator {
    /**
     * Generates the name of a new generated type, starting from a source type.
     *
     * <p>The generated type will have the same package as {@code samePackageElement}.
     * Its default simple name will be the simple name of {@code originatingType} with an additional,
     * implementation-dependent suffix.
     *
     * <p>In this example, if we consider {@code SourceClass} to be the originating type:
     * {@snippet :
     * class OuterClass {
     *     class MiddleClass {
     *         @ProcessedAnnotation
     *         record SourceClass(String field1, int field2) {}
     *     }
     * }
     *}
     * the default name of the generated type will be {@code OuterClass_MiddleClass_SourceClass<Suffix>}
     * (where {@code <Suffix>} is the implementation-dependent suffix).
     *
     * @param samePackageElement an element in the same package where the generated type will be located (can be of any
     *                           kind of element that has an associated package,
     *                           see {@link javax.lang.model.util.Elements#getPackageOf(Element)}).
     * @param originatingType    the type from which the generation of this type was triggered.
     * @param simpleNameOverride a type simple name that will be used instead of computing the default simple name as
     *                           explained above (if {@code null}, the default is used).
     * @return the generated type name
     */
    GeneratedTypeName generate(Element samePackageElement, TypeElement originatingType, String simpleNameOverride);

    /**
     * Variant of {@link #generate(Element, TypeElement, String)} that places the generated class in the
     * same package of {@code originatingType}.
     *
     * <p>Equivalent to {@code generate(originatingType, originatingType, simpleNameOverride)}
     */
    GeneratedTypeName generate(TypeElement originatingType, String simpleNameOverride);
}
