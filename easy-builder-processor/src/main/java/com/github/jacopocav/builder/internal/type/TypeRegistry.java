package com.github.jacopocav.builder.internal.type;

import java.lang.reflect.Type;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Keeps track of all types directly referenced in a generated java file
 */
public interface TypeRegistry {
    /**
     * Adds {@code type} to the referenced types
     */
    void register(Type type);

    /**
     * Registers {@code type } and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the simple name of {@code type}, if not ambiguous according to previously registered
     * types; the qualified name otherwise.
     */
    String getUsageName(Type type);

    /**
     * Registers {@code typeElement} and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the simple name of the type represented by {@code typeElement}, if not ambiguous according to previously registered
     * types; the qualified name otherwise.
     */
    String getUsageName(TypeElement typeElement);

    /**
     * Registers {@code mirror} and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the name of the type represented by {@code mirror}, using simple names when there's no ambiguity,
     * and qualified names otherwise.
     */
    String getUsageName(TypeMirror mirror);

    /**
     * Returns the qualified names of all types that have been registered, excluding all ambiguous types for which
     * {@link #getUsageName(Type)}, {@link #getUsageName(TypeMirror)} or {@link #getUsageName(TypeElement)} returns
     * a qualified name.
     */
    Collection<String> getSafeImports();
}
