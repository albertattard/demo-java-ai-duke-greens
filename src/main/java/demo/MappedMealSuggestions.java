package demo;

import static demo.Strings.requireNonBlank;

import module java.base;

record MappedMealSuggestions(List<MappedMealSuggestion> suggestions) implements MealRequestResult, Iterable<MappedMealSuggestion> {

    MappedMealSuggestions {
        if (suggestions == null || suggestions.isEmpty() || suggestions.size() > 7) {
            throw new IllegalArgumentException("A response must contain between one and seven suggestions");
        }
        suggestions = List.copyOf(suggestions);
    }

    @Override
    public Iterator<MappedMealSuggestion> iterator() {
        return suggestions.iterator();
    }
}

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
        if (preparationMinutes <= 0 || servings <= 0 || products == null || products.isEmpty() || estimatedCost == null) {
            throw new IllegalArgumentException("A complete mapped suggestion is required");
        }
        products = List.copyOf(products);
    }
}

record MappedProduct(Product product, int packageCount) {

    MappedProduct {
        if (product == null || packageCount <= 0) {
            throw new IllegalArgumentException("A mapped product needs a positive package count");
        }
    }
}
