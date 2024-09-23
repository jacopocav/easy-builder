package com.github.jacopocav.builder.processing.type;

import com.github.jacopocav.builder.internal.util.IterableUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.github.jacopocav.builder.internal.util.IterableUtils.getFirst;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

class TypeRegistryImpl implements TypeRegistry {
    private static final Type[] EXTENDS_OBJECT_BOUND = {Object.class};
    private final Map<String, LinkedHashSet<String>> ambiguitiesBySimpleName = new HashMap<>();

    public void register(Type type) {
        getUsageName(type);
    }

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

    public String getUsageName(TypeElement typeElement) {
        return switch (visit(typeElement)) {
            case SAFE -> typeElement.getSimpleName().toString();
            case AMBIGUOUS -> typeElement.getQualifiedName().toString();
        };
    }

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
