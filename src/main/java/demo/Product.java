package demo;

import static java.util.Objects.requireNonNull;

import static demo.Numbers.requireGreaterThanZero;
import static demo.Strings.requireMatches;
import static demo.Strings.requireNonBlank;

import module java.base;

/// A sellable catalogue product. Its stable lowercase kebab-case slug is the
/// public application reference used for model mapping and basket lines; names
/// are presentation data and must not be used for heuristic matching.
record Product(
        String slug,
        String name,
        int packageQuantity,
        MeasurementUnit packageUnit,
        BigDecimal price) {

    Product {
        requireValidSlug(slug);
        requireNonBlank(name, "The product name cannot be blank");
        requireGreaterThanZero(packageQuantity, "The package quantity must be greater than 0");
        requireNonNull(packageUnit, "The package unit must not be null");
        requireGreaterThanZero(price, "The product price must be greater than 0");
    }

    BigDecimal quantityForPackagesInBaseUnits(final int packageCount) {
        requireGreaterThanZero(packageCount, "The package count must be greater than 0");
        return packageUnit.toBaseUnits(BigDecimal.valueOf(packageQuantity))
                .multiply(BigDecimal.valueOf(packageCount));
    }

    static void requireValidSlug(final String slug) {
        requireMatches(slug, "[a-z0-9]+(?:-[a-z0-9]+)*", "A product slug must be lowercase kebab-case");
    }
}
