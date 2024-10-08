package com.github.jacopocav.builder.internal.type;

import static com.github.jacopocav.builder.internal.util.IterableUtils.getFirst;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import com.github.jacopocav.builder.internal.util.IterableUtils;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

/**
 * Keeps track of all types directly referenced in a generated java file
 */
public class TypeRegistry {
    private static final Type[] EXTENDS_OBJECT_BOUND = {Object.class};
    private final Map<String, LinkedHashSet<String>> ambiguitiesBySimpleName = new HashMap<>();

    /**
     * Adds {@code type} to the referenced types
     */
    public void register(Type type) {
        getUsageName(type);
    }

    /**
     * Registers {@code type } and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the simple name of {@code type}, if not ambiguous according to previously registered
     * types; the qualified name otherwise.
     */
    public String getUsageName(Type type) {
        if (type instanceof Class<?> cls) {
            if (cls.isPrimitive()) {
                return cls.getName();
            }
            if (cls.isArray()) {
                return getUsageName(cls.getComponentType()) + "[]";
            }

            return switch (visit(cls)) {
                case SAFE -> cls.getSimpleName();
                case AMBIGUOUS -> cls.getCanonicalName();
            };
        }

        if (type instanceof ParameterizedType pt) {
            var rawName = getUsageName(pt.getRawType());
            var typeArguments =
                    stream(pt.getActualTypeArguments()).map(this::getUsageName).collect(joining(", ", "<", ">"));

            return rawName + typeArguments;
        }

        if (type instanceof GenericArrayType gat) {
            return getUsageName(gat.getGenericComponentType()) + "[]";
        }

        if (type instanceof java.lang.reflect.WildcardType wt) {
            var lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length > 0) {
                return "? super " + stream(lowerBounds).map(this::getUsageName).collect(joining(" & "));
            }

            var upperBounds = wt.getUpperBounds();
            if (upperBounds.length == 0 || Arrays.equals(upperBounds, EXTENDS_OBJECT_BOUND)) {
                return "?";
            }

            return "? extends " + stream(upperBounds).map(this::getUsageName).collect(joining(" & "));
        }

        return type.getTypeName();
    }

    /**
     * Registers {@code typeElement} and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the simple name of the type represented by {@code typeElement}, if not ambiguous according to previously registered
     * types; the qualified name otherwise.
     */
    public String getUsageName(TypeElement typeElement) {
        return switch (visit(typeElement)) {
            case SAFE -> typeElement.getSimpleName().toString();
            case AMBIGUOUS -> typeElement.getQualifiedName().toString();
        };
    }

    /**
     * Registers {@code mirror} and returns a representation of it that's safe to use in source code
     * as a usage (in parameter/field/variable/return types, for example), while avoiding name clashes with other types.
     *
     * @return the name of the type represented by {@code mirror}, using simple names when there's no ambiguity,
     * and qualified names otherwise.
     */
    public String getUsageName(TypeMirror mirror) {
        return switch (mirror.getKind()) {
            case DECLARED -> {
                var declaredType = (DeclaredType) mirror;
                var typeElement = (TypeElement) declaredType.asElement();

                var name = getUsageName(typeElement);
                var typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    name += "<" + typeArguments.stream().map(this::getUsageName).collect(joining(", ")) + ">";
                }
                yield name;
            }

            case ARRAY -> getUsageName(((ArrayType) mirror).getComponentType()) + "[]";
            case WILDCARD -> {
                var wildcardType = (WildcardType) mirror;

                yield Optional.ofNullable(wildcardType.getExtendsBound())
                        .map(bound -> "? extends " + getUsageName(bound))
                        .or(() -> Optional.ofNullable(wildcardType.getSuperBound())
                                .map(bound -> "? super " + getUsageName(bound)))
                        .orElse("?");
            }

            default -> mirror.toString();
        };
    }

    /**
     * Returns the qualified names of all types that have been registered, excluding all ambiguous types for which
     * {@link #getUsageName(Type)}, {@link #getUsageName(TypeMirror)} or {@link #getUsageName(TypeElement)} returns
     * a qualified name.
     */
    public Collection<String> getSafeImports() {
        return ambiguitiesBySimpleName.values().stream()
                .map(IterableUtils::getFirst)
                .toList();
    }

    private VisitedType visit(TypeElement typeElement) {
        var ambiguities = ambiguitiesBySimpleName.computeIfAbsent(
                typeElement.getSimpleName().toString(), k -> new LinkedHashSet<>());
        var qualifiedName = typeElement.getQualifiedName().toString();
        ambiguities.add(qualifiedName);

        return isAmbiguous(ambiguities, qualifiedName) ? VisitedType.AMBIGUOUS : VisitedType.SAFE;
    }

    private VisitedType visit(Class<?> cls) {
        var ambiguities = ambiguitiesBySimpleName.computeIfAbsent(cls.getSimpleName(), k -> new LinkedHashSet<>());
        var qualifiedName = cls.getCanonicalName();
        ambiguities.add(qualifiedName);

        return isAmbiguous(ambiguities, qualifiedName) ? VisitedType.AMBIGUOUS : VisitedType.SAFE;
    }

    private boolean isAmbiguous(LinkedHashSet<String> ambiguities, String qualifiedName) {
        return ambiguities.size() > 1 && !getFirst(ambiguities).equals(qualifiedName);
    }

    private enum VisitedType {
        SAFE,
        AMBIGUOUS
    }
}
