package demo;

import module java.base;

final class Strings {

    static void requireNonBlank(final String value, final String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    static String formatPrice(final BigDecimal price, final Currency currency) {
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        formatter.setCurrency(currency);
        return formatter.format(price);
    }

    private Strings() { }
}