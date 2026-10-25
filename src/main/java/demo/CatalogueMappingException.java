package demo;

import module java.base;

/// Retains the correction constraint while exposing bounded, single-line server diagnostics.
final class CatalogueMappingException extends IllegalArgumentException {

    private final String diagnostic;

    CatalogueMappingException(final String reason, final String slug, final int mealIndex, final int ingredientIndex) {
        super("Every ingredient must name one distinct catalogue product");
        final String safeSlug = slug.codePoints().limit(160)
                .mapToObj(character -> Character.isISOControl(character) || character == '\\' || character == '\''
                        || character == 0x2028 || character == 0x2029
                        ? "\\u%04x".formatted(character)
                        : new String(Character.toChars(character)))
                .collect(Collectors.joining());
        this.diagnostic = "reason=%s mealIndex=%d ingredientIndex=%d productSlug='%s%s'"
                .formatted(reason, mealIndex, ingredientIndex, safeSlug, slug.codePointCount(0, slug.length()) > 160 ? "..." : "");
    }

    String diagnostic() {
        return diagnostic;
    }
}
