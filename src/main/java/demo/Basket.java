package demo;

import static java.util.Objects.requireNonNull;

import static demo.Collections.requireContainsKey;
import static demo.Numbers.requireNonNegative;
import static demo.Product.requireValidSlug;

import module java.base;

/// Session-scoped, application-owned basket quantities expressed as whole
/// packs.
record Basket(Map<String, Integer> quantities) {

    Basket {
        requireNonNull(quantities, "Basket quantities are required");
        quantities = Map.copyOf(quantities);
        if (quantities.entrySet().stream().anyMatch(entry -> Strings.isBlank(entry.getKey()) || entry.getValue() == null || entry.getValue() <= 0)) {
            throw new IllegalArgumentException("Basket quantities must be positive whole packs");
        }
    }

    static Basket empty() {
        return new Basket(Map.of());
    }

    Basket addRequirements(final List<MappedMealSuggestion> selectedMeals) {
        final Map<String, Integer> updated = new HashMap<>(quantities);
        requiredPackages(selectedMeals)
                .forEach((slug, count) -> updated.merge(slug, count, Math::max));
        return new Basket(updated);
    }

    Basket changeQuantity(final String slug, final int quantity) {
        requireValidSlug(slug);
        requireNonNegative(quantity, "The quantity must be 0 to remove from basket or greater");
        requireContainsKey(quantities, slug, "The slug must exist in the basket to be update");

        final Map<String, Integer> updated = new HashMap<>(quantities);
        if (quantity <= 0) {
            updated.remove(slug);
        } else {
            updated.put(slug, quantity);
        }

        return new Basket(updated);
    }

    int quantityOf(final String slug) {
        return quantities.getOrDefault(slug, 0);
    }

    boolean isEmpty() {
        return quantities.isEmpty();
    }

    boolean fulfils(final List<MappedMealSuggestion> selectedMeals) {
        return requiredPackages(selectedMeals).entrySet().stream()
                .allMatch(requirement -> quantityOf(requirement.getKey()) >= requirement.getValue());
    }

    BigDecimal totalPrice(final List<Product> products) {
        final Map<String, Product> productsBySlug = products.stream()
                .collect(Collectors.toMap(Product::slug, Function.identity()));
        return quantities.entrySet().stream()
                .map(entry -> productsBySlug.get(entry.getKey()).price().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /// Adds each selected meal’s base-unit ingredient requirements by product,
    /// then rounds each combined amount up once to the smallest whole-pack
    /// quantity that covers it. This prevents shared ingredients from being
    /// rounded separately for every meal and avoids adding more packs than the
    /// combined requirement needs.
    private static Map<String, Integer> requiredPackages(final List<MappedMealSuggestion> selectedMeals) {
        final Map<Product, BigDecimal> requiredQuantities = new HashMap<>();
        selectedMeals.stream().flatMap(meal -> meal.products().stream())
                .forEach(product -> requiredQuantities.merge(product.product(), product.requiredQuantity(), BigDecimal::add));
        return requiredQuantities.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().slug(),
                entry -> entry.getValue().divide(entry.getKey().quantityForPackagesInBaseUnits(1), 0, RoundingMode.CEILING).intValueExact()));
    }
}
