package demo;

import static java.util.Objects.requireNonNull;

import static demo.Numbers.requireNonNegative;
import static demo.Product.requireValidSlug;
import static demo.Strings.requireNonBlank;

import module java.base;

sealed interface MealRequestSessionState permits SuccessfulMealRequest, FailedMealRequest {
}

record SuccessfulMealRequest(
        String request,
        MappedMealSuggestions suggestions,
        Set<Integer> selectedMealIndexes,
        Basket basket) implements MealRequestSessionState {

    SuccessfulMealRequest {
        requireNonBlank(request, "The request cannot be blank");
        requireNonNull(suggestions, "Suggestions are required");
        selectedMealIndexes = Set.copyOf(selectedMealIndexes);
        requireNonNull(basket, "A basket is required");
    }

    SuccessfulMealRequest(final String request, final MappedMealSuggestions suggestions) {
        this(request, suggestions, Set.of(), Basket.empty());
    }

    SuccessfulMealRequest addMeal(final int index) {
        if (index < 0 || index >= suggestions.suggestions().size() || selectedMealIndexes.contains(index)) {
            return this;
        }

        final Set<Integer> updatedIndexes = new HashSet<>(selectedMealIndexes);
        updatedIndexes.add(index);
        final List<MappedMealSuggestion> selected = updatedIndexes.stream()
                .sorted()
                .map(position -> suggestions.suggestions().get(position))
                .toList();

        return new SuccessfulMealRequest(request,
                suggestions,
                updatedIndexes,
                basket.addRequirements(selected));
    }

    SuccessfulMealRequest changeBasketQuantity(final String slug, final int quantity) {
        requireValidSlug(slug);
        requireNonNegative(quantity, "The quantity must be 0 to remove from basket or greater");
        return new SuccessfulMealRequest(request, suggestions, selectedMealIndexes, basket.changeQuantity(slug, quantity));
    }

    List<MappedMealSuggestion> selectedMeals() {
        return selectedMealIndexes.stream().sorted().map(index -> suggestions.suggestions().get(index)).toList();
    }

    boolean needsResetConfirmation() {
        return !selectedMealIndexes.isEmpty() || !basket.isEmpty();
    }
}

record FailedMealRequest(String request) implements MealRequestSessionState {

    FailedMealRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}
