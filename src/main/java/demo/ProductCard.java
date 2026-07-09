package demo;

import static demo.Strings.formatPrice;
import static demo.Strings.requireNonBlank;

record ProductCard(String name, String packageDetail, String formattedPrice) {

    ProductCard {
        requireNonBlank(name, "The product name cannot be blank");
        requireNonBlank(packageDetail, "The product package details cannot be blank");
        requireNonBlank(formattedPrice, "The product price cannot be blank");
    }

    static ProductCard of(final Product product) {
        return new ProductCard(
                product.name(),
                product.packageQuantity() + " " + product.packageUnit().symbol(),
                formatPrice(product.price()));
    }
}
