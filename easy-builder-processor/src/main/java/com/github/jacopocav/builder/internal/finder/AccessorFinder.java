package com.github.jacopocav.builder.internal.finder;

import com.github.jacopocav.builder.internal.util.StringUtils;
import com.github.jacopocav.builder.internal.TargetClassRetriever;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class AccessorFinder implements Function<VariableElement, Accessor> {
    private final Types types;
    private final Elements elements;
    private final TargetClassRetriever targetClassRetriever;

    public AccessorFinder(Types types, Elements elements, TargetClassRetriever targetClassRetriever) {
        this.types = types;
        this.elements = elements;
        this.targetClassRetriever = targetClassRetriever;
    }

    /**
     * Finds an {@link Accessor} (i.e. getter method or field) for the given constructor or method
     * argument.
     * @param argument the {@link CreatorMethodFinder creator method} argument
     * @return either {@link Accessor.Found} or
     * {@link Accessor.NotFound}
     */
    @Override
    public Accessor apply(VariableElement argument) {
        var callerPackage = elements.getPackageOf(argument).getQualifiedName();
        var targetClass = targetClassRetriever.getElement(argument);

        return findAccessorRecursively(targetClass, argument, callerPackage)
                .orElseGet(() -> new Accessor.NotFound(argument));
    }

    /**
     * Finds a valid accessor in the {@code declaringClass} recursively, following this logic:
     * <ol>
     *     <li>Finds a valid getter in the current class ({@code declaringClass})
     *     <li>Otherwise, finds a valid field in the current class
     *     <li>Otherwise, repeats the search in the superclass (if present)
     * </ol>
     *
     * The search stops if the accessor has been found, or if no superclass exists for
     * the current class.
     */
    private Optional<Accessor> findAccessorRecursively(
            TypeElement declaringClass, VariableElement argument, Name callerPackage) {

        return findAccessorMethod(declaringClass, argument, callerPackage)
                .or(() -> findAccessorField(declaringClass, argument, callerPackage))
                .or(() -> getSuperClass(declaringClass)
                        .flatMap(superType -> findAccessorRecursively(superType, argument, callerPackage)));
    }

    /**
     * Finds a valid getter method in the {@code declaringClass}.
     * The getter must:
     * <ul>
     *     <li>be accessible from {@code callerPackage} (i.e. must not be {@code private} if in the same
     *     package, otherwise it must be {@code public})
     *     <li>have a return type assignable to the {@code argument} type
     *     <li>have a "standard" getter name, i.e. it must satisfy one of the following:
     *     <ul>
     *         <li>it has the same name as {@code argument} (e.g. record getters)
     *         <li>it has the name as {@code argument}, capitalized and prefixed with {@code get}
     *         <li>it has the name as {@code argument}, capitalized and prefixed with {@code is}
     *         (only if {@code argument} is {@code boolean}/{@code Boolean}
     *     </ul>
     * </ul>
     */
    private Optional<Accessor> findAccessorMethod(
            TypeElement declaringClass, VariableElement argument, Name callerPackage) {
        return methodsIn(declaringClass.getEnclosedElements()).stream()
                .filter(this::isNotStatic)
                .filter(method -> isAccessible(method, callerPackage))
                .filter(method -> hasCompatibleReturnType(argument, method))
                .filter(method -> hasStandardAccessorName(argument, method))
                .findFirst()
                .map(accessor -> new Accessor.Found(argument, accessor));
    }

    /**
     * Finds a valid field in the {@code declaringClass}.
     * The field must:
     * <ul>
     *     <li>be accessible from {@code callerPackage} (i.e. must not be {@code private} if in the same
     *     package, otherwise it must be {@code public})
     *     <li>have a type assignable to the {@code argument} type
     *     <li>have the same name as {@code argument}
     * </ul>
     */
    private Optional<Accessor> findAccessorField(
            TypeElement declaringClass, VariableElement argument, Name callerPackage) {
        return fieldsIn(declaringClass.getEnclosedElements()).stream()
                .filter(this::isNotStatic)
                .filter(field -> isAccessible(field, callerPackage))
                .filter(field -> hasCompatibleType(argument, field))
                .filter(field -> hasSameName(argument, field))
                .findFirst()
                .map(accessor -> new Accessor.Found(argument, accessor));
    }

    private boolean hasSameName(VariableElement argument, VariableElement field) {
        return argument.getSimpleName().equals(field.getSimpleName());
    }

    private boolean isNotStatic(Element element) {
        return !element.getModifiers().contains(STATIC);
    }

    private boolean isAccessible(Element method, Name callerPackage) {
        var inSamePackageAsCaller =
                elements.getPackageOf(method).getQualifiedName().equals(callerPackage);

        return inSamePackageAsCaller
                ? !method.getModifiers().contains(PRIVATE)
                : method.getModifiers().contains(PUBLIC);
    }

    private boolean hasCompatibleReturnType(VariableElement argument, ExecutableElement method) {
        return types.isAssignable(method.getReturnType(), argument.asType());
    }

    private boolean hasCompatibleType(VariableElement argument, VariableElement field) {
        return types.isAssignable(field.asType(), argument.asType());
    }

    private boolean hasStandardAccessorName(VariableElement argument, ExecutableElement method) {
        var methodName = method.getSimpleName();
        var argumentName = argument.getSimpleName().toString();

        return methodName.contentEquals(argumentName)
                || methodName.contentEquals("get" + StringUtils.capitalize(argumentName))
                || (isBoolean(argument) && methodName.contentEquals("is" + StringUtils.capitalize(argumentName)));
    }

    private boolean isBoolean(VariableElement argument) {
        return types.isAssignable(argument.asType(), types.getPrimitiveType(BOOLEAN));
    }

    private Optional<TypeElement> getSuperClass(TypeElement type) {
        return Optional.of(type.getSuperclass())
                .filter(cls -> cls.getKind() != NONE)
                .map(types::asElement)
                .map(TypeElement.class::cast);
    }
}
