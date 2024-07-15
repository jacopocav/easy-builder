package com.github.jacopocav.builder.util;

import java.util.stream.IntStream;

public class SourceUtils {
    private SourceUtils() {}

    public record ErrorPosition(int line, int column) {}

    public static ErrorPosition firstPositionThatContains(String source, String searchTarget) {
        var lines = source.lines().toList();

        var line = IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).contains(searchTarget))
                .map(i -> i + 1)
                .findFirst()
                .orElseThrow();

        var column = lines.get(line - 1).indexOf(searchTarget) + 1;

        return new ErrorPosition(line, column);
    }
}
