package demo;

import module java.base;

final class Numbers {

    static void requireBetween(final int value, final int lowerBound, final int upperBound, final String message) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("The lower bound must be smaller than the upper bound");
        }

        if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException(message);
        }
    }

    static void requireNonNegative(final int value, final String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

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

    private Numbers() {}
}
