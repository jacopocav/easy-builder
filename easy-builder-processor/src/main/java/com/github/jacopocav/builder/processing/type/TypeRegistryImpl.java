package com.github.jacopocav.builder.processing.type;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

class TypeRegistryImpl implements TypeRegistry {
    private static final Type[] EXTENDS_OBJECT_BOUND = {Object.class};
    private final Map<String, SequencedSet<String>> ambiguitiesBySimpleName = new HashMap<>();

    public void register(Type type) {
        getUsageName(type);
    }

    public String getUsageName(Type type) {
        return switch (type) {
            case Class<?> cls
                    when cls.isPrimitive() -> cls.getName();
            case Class<?> cls
                    when cls.isArray() -> getUsageName(cls.getComponentType()) + "[]";
            case Class<?> cls -> switch (visit((Class<?>) type)) {
                case SAFE -> cls.getSimpleName();
                case AMBIGUOUS -> cls.getName();
            };

            case ParameterizedType pt -> {
                var rawName = getUsageName(pt.getRawType());
                var typeArguments = stream(pt.getActualTypeArguments())
                        .map(this::getUsageName)
                        .collect(joining(", ", "<", ">"));

                yield rawName + typeArguments;
            }

            case GenericArrayType gat -> getUsageName(gat.getGenericComponentType()) + "[]";

            case java.lang.reflect.WildcardType wt -> {
                var lowerBounds = wt.getLowerBounds();
                if (lowerBounds.length > 0) {
                    yield "? super " + stream(lowerBounds)
                            .map(this::getUsageName)
                            .collect(joining(" & "));
                }

                var upperBounds = wt.getUpperBounds();
                if (upperBounds.length == 0 || Arrays.equals(upperBounds, EXTENDS_OBJECT_BOUND)) {
                    yield "?";
                }

                yield "? extends " + stream(upperBounds)
                        .map(this::getUsageName)
                        .collect(joining(" & "));
            }

            default -> type.getTypeName();
        };
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
                    name += "<"
                            + typeArguments.stream().map(this::getUsageName).collect(joining(", ")) + ">";
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
                .map(SequencedCollection::getFirst)
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
        var qualifiedName = cls.getName();
        ambiguities.add(qualifiedName);

        return isAmbiguous(ambiguities, qualifiedName) ? VisitedType.AMBIGUOUS : VisitedType.SAFE;
    }

    private boolean isAmbiguous(SequencedSet<String> ambiguities, String qualifiedName) {
        return ambiguities.size() > 1 && !ambiguities.getFirst().equals(qualifiedName);
    }

    private enum VisitedType {
        SAFE,
        AMBIGUOUS
    }
}
