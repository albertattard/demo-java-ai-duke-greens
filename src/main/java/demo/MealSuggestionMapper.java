package demo;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;

import static demo.Collections.requireNonEmpty;

import module java.base;

/**
 * Maps against the same catalogue snapshot supplied to the model. The snapshot
 * is indexed once by slug, avoiding a catalogue lookup for every returned
 * ingredient while ensuring validation uses the exact set of products the model
 * was allowed to select.
 */
@Component
class MealSuggestionMapper {

    MappedMealSuggestions map(final ModelMealSuggestions modelSuggestions, final List<Product> products) {
        requireNonNull(modelSuggestions, "The model meal suggestions cannot be null");
        requireNonEmpty(products, "The catalogue cannot be empty");

        final Map<String, Product> productsBySlug = products.stream()
                .collect(Collectors.toUnmodifiableMap(Product::slug, Function.identity()));

        final List<MappedMealSuggestion> mappedSuggestions = modelSuggestions.suggestions().stream()
                .map(suggestion -> mapSuggestion(suggestion, productsBySlug))
                .toList();

        return new MappedMealSuggestions(mappedSuggestions);
    }

    private MappedMealSuggestion mapSuggestion(final ModelMealSuggestion suggestion, final Map<String, Product> productsBySlug) {
        final Set<String> seenSlugs = new HashSet<>();
        final List<MappedProduct> mappedProducts = suggestion.ingredients().stream()
                .map(ingredient -> mapIngredient(ingredient, productsBySlug, seenSlugs))
                .toList();
        final BigDecimal estimatedCost = mappedProducts.stream()
                .map(mapped -> mapped.product().price().multiply(BigDecimal.valueOf(mapped.packageCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MappedMealSuggestion(suggestion.name(), suggestion.preparationMinutes(), suggestion.explanation(),
                suggestion.servings(), mappedProducts, estimatedCost);
    }

    private MappedProduct mapIngredient(
            final ModelIngredient ingredient,
            final Map<String, Product> productsBySlug,
            final Set<String> seenSlugs) {
        final Product product = productsBySlug.get(ingredient.productSlug());
        if (product == null || !seenSlugs.add(ingredient.productSlug())) {
            throw new IllegalArgumentException("Every ingredient must name one distinct catalogue product");
        }
        if (!ingredient.quantity().matches("[1-9][0-9]{0,4}")) {
            throw new IllegalArgumentException("Ingredient quantities must be positive whole integers between 1 and 99999 (five 9s)");
        }
        final MeasurementUnit ingredientUnit = MeasurementUnit.from(ingredient.unit());
        final MeasurementUnit packageUnit = product.packageUnit();
        if (!ingredientUnit.hasSameDimensionAs(packageUnit)) {
            throw new IllegalArgumentException("Mass and volume cannot be combined");
        }
        final BigDecimal requiredQuantity = ingredientUnit.toBaseUnits(new BigDecimal(ingredient.quantity()));
        final BigDecimal packageQuantity = packageUnit.toBaseUnits(BigDecimal.valueOf(product.packageQuantity()));
        final int packageCount = requiredQuantity.divide(packageQuantity, 0, RoundingMode.CEILING)
                .intValueExact();
        return new MappedProduct(product, packageCount);
    }

}
