package demo;

import static demo.Strings.formatPrice;

import module java.base;

record BasketLineCard(
        String slug,
        String name,
        String packageDetail,
        int quantity,
        String formattedLineTotal) {

    static BasketLineCard of(final Product product, final int quantity) {
        return new BasketLineCard(product.slug(),
                product.name(),
                product.packageQuantity() + " " + product.packageUnit().symbol(),
                quantity,
                formatPrice(product.price().multiply(BigDecimal.valueOf(quantity))));
    }
}
