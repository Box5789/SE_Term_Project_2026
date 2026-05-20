package kr.ac.se.issuemanager.service;

import java.util.Collection;
import java.util.function.Function;

final class IdGenerator {
    private IdGenerator() {
    }

    static <T> String nextId(String prefix, Collection<T> items, Function<T, String> idGetter) {
        int max = items.stream()
                .map(idGetter)
                .filter(id -> id != null && id.startsWith(prefix + "-"))
                .map(id -> id.substring(prefix.length() + 1))
                .mapToInt(IdGenerator::parseNumber)
                .max()
                .orElse(0);
        return "%s-%03d".formatted(prefix, max + 1);
    }

    private static int parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}

