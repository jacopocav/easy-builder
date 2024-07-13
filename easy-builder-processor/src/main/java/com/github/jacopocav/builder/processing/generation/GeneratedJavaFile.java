package com.github.jacopocav.builder.processing.generation;

/**
 * In-memory representation of a generated Java file, ready to be written to file.
 *
 * @param qualifiedName the qualified name of the main top-level class contained in the file
 * @param sourceCode    the java source code contained in the file
 */
public record GeneratedJavaFile(String qualifiedName, String sourceCode) {}
