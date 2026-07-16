package demo;

import java.util.List;

import static demo.Strings.formatPrice;

/// Template-facing representation of a mapped meal suggestion.
public record MealSuggestionCard(
        int resultSet,
        int index,
        String name,
        String formattedPreparationTime,
        String explanation,
        int servings,
        List<MealProductCard> products,
        String formattedEstimatedCost) {

    static MealSuggestionCard of(final int index, final MappedMealSuggestion suggestion) {
        return of(0, index, suggestion);
    }

    static MealSuggestionCard of(final int resultSet, final int index, final MappedMealSuggestion suggestion) {
        return new MealSuggestionCard(
                resultSet,
                index,
                suggestion.name(),
                formatPreparationTime(suggestion.preparationMinutes()),
                suggestion.explanation(),
                suggestion.servings(),
                suggestion.products().stream().map(MealProductCard::of).toList(),
                formatPrice(suggestion.estimatedCost()));
    }

    // TODO: Consider moving this to the Strings class
    static String formatPreparationTime(final int minutes) {
        final int hours = minutes / 60;
        final int remainingMinutes = minutes % 60;
        if (hours == 0) {
            return minutes + " min";
        }
        if (remainingMinutes == 0) {
            return hours + " hr";
        }
        return hours + " hr " + remainingMinutes + " min";
    }
}
