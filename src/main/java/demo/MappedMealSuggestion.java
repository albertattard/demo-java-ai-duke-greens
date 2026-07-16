package demo;

import module java.base;

import static demo.Collections.requireNonEmpty;
import static demo.Numbers.requireGreaterThanZero;
import static demo.Strings.requireNonBlank;
import static java.util.Objects.requireNonNull;

record MappedMealSuggestion(
        String name,
        int preparationMinutes,
        String explanation,
        int servings,
        List<MappedProduct> products,
        BigDecimal estimatedCost) {

    MappedMealSuggestion {
        requireNonBlank(name, "A suggestion name is required");
        requireNonBlank(explanation, "A suggestion explanation is required");
        requireGreaterThanZero(preparationMinutes, "The preparation time must be greater than 0");
        requireGreaterThanZero(servings, "The meal serving must be greater than 0");
        requireNonEmpty(products, "The meal related products must not be empty");
        requireGreaterThanZero(estimatedCost, "The meal estimated cost must be greater than 0");

        products = List.copyOf(products);
    }
}

record MappedProduct(
        Product product,
        BigDecimal requiredQuantity,
        int packageCount) {

    MappedProduct {
        requireNonNull(product, "The mapped product must not be null");
        requireGreaterThanZero(requiredQuantity, "The required quantity must be greater than 0");
        requireGreaterThanZero(packageCount, "The package count must be greater than 0");
    }

    MappedProduct(final Product product, final int packageCount) {
        this(product, product.quantityForPackagesInBaseUnits(packageCount), packageCount);
    }
}
