package com.github.jacopocav.builder.util;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.assertj.core.api.AbstractClassAssert;
import org.assertj.core.api.Assertions;

public abstract class AbstractRichClassAssert<SELF extends AbstractRichClassAssert<SELF>>
        extends AbstractClassAssert<SELF> {
    public AbstractRichClassAssert(Class<?> actual, Class<SELF> selfType) {
        super(actual, selfType);
    }

    public SELF hasDeclaredField(Modifier modifier, Type type, String name) {
        return hasDeclaredField(Set.of(modifier), type, name);
    }

    public SELF hasDeclaredField(Collection<Modifier> modifiers, Type type, String name) {
        var fieldSignature = fieldSignature(modifiers, type, name);
        try {
            var field = actual.getDeclaredField(name);

            Assertions.assertThat(field.getGenericType())
                    .withFailMessage(() -> "field %s is not of type %s (actual: %s)"
                            .formatted(field.getName(), type.getTypeName(), field.getGenericType()))
                    .isEqualTo(type);

            assertModifiersMatch(field, modifiers, field.getModifiers());

        } catch (NoSuchFieldException e) {
            throw new AssertionError("field %s is not present in class %s".formatted(fieldSignature, actual));
        }

        return myself;
    }

    public SELF hasDeclaredMethod(Modifier modifier, Type returnType, String name, Type... parameterTypes) {
        return hasDeclaredMethod(Set.of(modifier), returnType, name, parameterTypes);
    }

    public SELF hasDeclaredMethod(
            Collection<Modifier> modifiers, Type returnType, String name, Type... parameterTypes) {

        var method = Arrays.stream(actual.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .filter(m -> returnType.equals(m.getReturnType()))
                .filter(m -> Arrays.equals(parameterTypes, m.getGenericParameterTypes()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("class %s does not declare method %s"
                        .formatted(actual.getName(), methodSignature(modifiers, returnType, name, parameterTypes))));

        assertModifiersMatch(method, modifiers, method.getModifiers());

        return myself;
    }

    public SELF hasDeclaredConstructor(Modifier modifier, Type... parameterTypes) {
        return hasDeclaredConstructor(Set.of(modifier), parameterTypes);
    }

    public SELF hasDeclaredConstructor(Collection<Modifier> modifiers, Type... parameterTypes) {
        var constructor = Arrays.stream(actual.getDeclaredConstructors())
                .filter(m -> Arrays.equals(parameterTypes, m.getGenericParameterTypes()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("class %s does not declare method %s"
                        .formatted(actual.getName(), constructorSignature(modifiers, parameterTypes))));

        assertModifiersMatch(constructor, modifiers, constructor.getModifiers());
        return myself;
    }

    private void assertModifiersMatch(Object element, Collection<Modifier> modifiers, int modifiersInt) {
        var actualModifiers = Arrays.stream(
                        java.lang.reflect.Modifier.toString(modifiersInt).split(" "))
                .filter(not(String::isBlank))
                .map(String::toUpperCase)
                .map(Modifier::valueOf)
                .collect(toUnmodifiableSet());

        Assertions.assertThat(actualModifiers)
                .withFailMessage(() -> "Expecting actual [%s] to have exactly modifiers '%s' but does not"
                        .formatted(element, getModifierString(modifiers)))
                .containsExactlyInAnyOrderElementsOf(modifiers);
    }

    private static String fieldSignature(Collection<Modifier> modifiers, Type returnType, String name) {
        var modifierString = getModifierString(modifiers);

        return "%s %s %s".formatted(modifierString, returnType.getTypeName(), name);
    }

    private static String methodSignature(
            Collection<Modifier> modifiers, Type returnType, String name, Type[] parameterTypes) {
        var modifierString = getModifierString(modifiers);

        var parameterString = getParameterString(parameterTypes);

        return "%s %s %s(%s)".formatted(modifierString, returnType.getTypeName(), name, parameterString);
    }

    private String constructorSignature(Collection<Modifier> modifiers, Type[] parameterTypes) {
        var modifierString = getModifierString(modifiers);
        var parameterString = getParameterString(parameterTypes);

        return "%s %s(%s)".formatted(modifierString, actual.getName(), parameterString);
    }

    private static String getParameterString(Type[] parameterTypes) {
        return Arrays.stream(parameterTypes).map(Type::getTypeName).collect(joining(", "));
    }

    private static String getModifierString(Collection<Modifier> modifiers) {
        return modifiers.stream().sorted(naturalOrder()).map(Modifier::toString).collect(joining(" "));
    }
}
