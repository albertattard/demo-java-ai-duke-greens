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

    private Strings() { }
}
