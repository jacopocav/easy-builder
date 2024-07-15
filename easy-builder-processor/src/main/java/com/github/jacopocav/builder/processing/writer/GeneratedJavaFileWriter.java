package com.github.jacopocav.builder.processing.writer;

import com.github.jacopocav.builder.processing.generation.GeneratedJavaFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.annotation.processing.Filer;

/**
 * Writes generated java sources to file.
 */
public class GeneratedJavaFileWriter {
    private final Filer filer;

    public GeneratedJavaFileWriter(Filer filer) {
        this.filer = filer;
    }

    /**
     * Writes {@code generatedJavaFile} to a new file using {@link Filer}
     * @throws UncheckedIOException if any {@link IOException} is thrown while writing to file
     */
    public void write(GeneratedJavaFile generatedJavaFile) throws UncheckedIOException {
        try {
            var javaFileObject = filer.createSourceFile(generatedJavaFile.qualifiedName());
            try (var writer = javaFileObject.openWriter()) {
                writer.write(generatedJavaFile.sourceCode());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
