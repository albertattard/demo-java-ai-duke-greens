package demo;

import java.util.List;

import static demo.Strings.formatPrice;

/**
 * Template-facing representation of a mapped meal suggestion.
 */
public record MealSuggestionCard(
        String name,
        int preparationMinutes,
        String explanation,
        int servings,
        List<MealProductCard> products,
        String formattedEstimatedCost) {

    static MealSuggestionCard of(final MappedMealSuggestion suggestion) {
        return new MealSuggestionCard(
                suggestion.name(),
                suggestion.preparationMinutes(),
                suggestion.explanation(),
                suggestion.servings(),
                suggestion.products().stream().map(MealProductCard::of).toList(),
                formatPrice(suggestion.estimatedCost()));
    }
}
