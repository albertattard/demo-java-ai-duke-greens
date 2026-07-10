package demo;

import module java.base;

final class Collections {

    static void requireNonEmpty(final Collection<?> collection, final String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    static void requireSizeBetween(final Collection<?> collection, final int lowerBound, final int upperBound, final String message) {
        if (lowerBound < 0 || lowerBound > upperBound) {
            throw new IllegalArgumentException("Invalid size bounds");
        }

        if (collection == null) {
            throw new IllegalArgumentException(message);
        }

        final int size = collection.size();
        if (size < lowerBound || size > upperBound) {
            throw new IllegalArgumentException(message);
        }
    }

    static <T> void requireContainsKey(final Map<T, ?> map, final T key, final String message) {
        if (map == null || key == null || !map.containsKey(key)) {
            throw new IllegalArgumentException(message);
        }
    }

    private Collections() { }
}
