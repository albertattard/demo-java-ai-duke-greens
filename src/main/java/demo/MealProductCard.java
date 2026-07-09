package demo;

import static demo.Strings.formatPrice;

/**
 * Template-facing representation of a catalogue product required by a meal.
 */
public record MealProductCard(
        int packageCount,
        String name,
        String packageDetail,
        String formattedPrice) {

    static MealProductCard of(final MappedProduct mappedProduct) {
        final Product product = mappedProduct.product();
        return new MealProductCard(
                mappedProduct.packageCount(),
                product.name(),
                product.packageQuantity() + " " + product.packageUnit().symbol(),
                formatPrice(product.price()));
    }
}
