package demo;

import module java.base;
import org.springframework.stereotype.Component;

import static demo.Collections.requireNonEmpty;
import static java.util.Objects.requireNonNull;

/// Maps against the same catalogue snapshot supplied to the model. The snapshot
/// is indexed once by slug, avoiding a catalogue lookup for every returned
/// ingredient while ensuring validation uses the exact set of products the
/// model was allowed to select. Slug lookup is deliberately exact: do not trim,
/// case-fold, or heuristically match product names, because the application—not
/// the model—is authoritative for the sellable product, package, and price.
@Component
class MealSuggestionMapper {

    List<MappedMealSuggestion> map(final ModelMealSuggestions modelSuggestions, final List<Product> products) {
        requireNonNull(modelSuggestions, "The model meal suggestions cannot be null");
        requireNonEmpty(products, "The catalogue cannot be empty");

        final Map<String, Product> productsBySlug = products.stream()
                .collect(Collectors.toUnmodifiableMap(Product::slug, Function.identity()));

        final List<MappedMealSuggestion> mappedSuggestions = IntStream.range(0, modelSuggestions.suggestions().size())
                .mapToObj(index -> mapSuggestion(modelSuggestions.suggestions().get(index), productsBySlug, index + 1))
                .toList();

        return List.copyOf(mappedSuggestions);
    }

    private MappedMealSuggestion mapSuggestion(final ModelMealSuggestion suggestion, final Map<String, Product> productsBySlug,
                                             final int mealIndex) {
        final Set<String> seenSlugs = new HashSet<>();
        final List<MappedProduct> mappedProducts = IntStream.range(0, suggestion.ingredients().size())
                .mapToObj(index -> mapIngredient(suggestion.ingredients().get(index), productsBySlug, seenSlugs, mealIndex, index + 1))
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
            final Set<String> seenSlugs,
            final int mealIndex,
            final int ingredientIndex) {
        final Product product = productsBySlug.get(ingredient.productSlug());
        if (product == null) {
            throw new CatalogueMappingException("unknown_slug", ingredient.productSlug(), mealIndex, ingredientIndex);
        }
        if (!seenSlugs.add(ingredient.productSlug())) {
            throw new CatalogueMappingException("duplicate_slug", ingredient.productSlug(), mealIndex, ingredientIndex);
        }
        if (!ingredient.quantity().matches("[1-9][0-9]{0,4}")) {
            throw new IllegalArgumentException("Ingredient quantities must be positive whole integers between 1 and 99999 (five 9s)");
        }

        // Validate units before conversion: mass cannot fulfil a volume pack,
        // or vice versa. Retaining the requirement in base units lets the
        // basket aggregate shared ingredients before it rounds to whole packs.
        final MeasurementUnit ingredientUnit = MeasurementUnit.from(ingredient.unit());
        final MeasurementUnit packageUnit = product.packageUnit();
        if (!ingredientUnit.hasSameDimensionAs(packageUnit)) {
            throw new IllegalArgumentException("Mass and volume cannot be combined");
        }
        final BigDecimal requiredQuantity = ingredientUnit.toBaseUnits(new BigDecimal(ingredient.quantity()));
        final BigDecimal packageQuantity = packageUnit.toBaseUnits(BigDecimal.valueOf(product.packageQuantity()));
        final int packageCount = requiredQuantity.divide(packageQuantity, 0, RoundingMode.CEILING)
                .intValueExact();
        return new MappedProduct(product, requiredQuantity, packageCount);
    }

}
