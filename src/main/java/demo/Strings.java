package demo;

import module java.base;

final class Strings {

    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Locale LOCALE = Locale.GERMANY;

    static void requireMatches(final String value, final String regex, final String message) {
        if (isBlank(value) || !value.matches(regex)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void requireNonBlank(final String value, final String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    static String requireLengthBetween(final String value, int lowerBound, int upperBound, final String message) {
        if (isLengthBetween(value, lowerBound, upperBound)) {
            return value;
        }

        throw new IllegalArgumentException(message);
    }

    static boolean isLengthBetween(final String value, int lowerBound, int upperBound) {
        if (lowerBound < 0 || lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must be greater than 0 and less than upper bound");
        }

        return isNotBlank(value)
                && value.length() >= lowerBound
                && value.length() <= upperBound;
    }

    static boolean isNotBlank(final String value) {
        return !isBlank(value);
    }

    static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    /// The catalogue has one EUR price list. Render it using German conventions
    /// only; formatting does not convert the underlying price to another
    /// currency.
    static String formatPrice(final BigDecimal price) {
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE);
        formatter.setCurrency(CURRENCY);
        return formatter.format(price);
    }

    private Strings() {}
}
