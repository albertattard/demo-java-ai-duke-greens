package demo;

import module java.base;

final class Numbers {

    static void requireGreaterThanZero(final int value, final String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    static void requireGreaterThanZero(final BigDecimal value, final String message) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private Numbers() { }
}
