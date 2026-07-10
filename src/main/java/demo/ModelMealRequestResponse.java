package demo;

import static java.util.Objects.requireNonNull;

import static demo.Collections.requireNonEmpty;
import static demo.Collections.requireSizeBetween;
import static demo.Numbers.requireGreaterThanZero;
import static demo.Strings.requireNonBlank;

import module java.base;

record ModelMealRequestResponse(MealRequestScope scope, List<ModelMealSuggestion> suggestions) {

    ModelMealRequestResponse {
        requireNonNull(scope, "A request scope is required");
        requireNonNull(suggestions, "A response must contain a suggestions list");
        suggestions = List.copyOf(suggestions);

        if (scope == MealRequestScope.OUT_OF_SCOPE && !suggestions.isEmpty()) {
            throw new IllegalArgumentException("Out-of-scope requests cannot contain meal suggestions");
        }

        if (scope == MealRequestScope.IN_SCOPE) {
            requireSizeBetween(suggestions, 1, 7, "A response must contain between one and seven suggestions");
        }
    }

    static ModelMealRequestResponse inScope(final ModelMealSuggestions suggestions) {
        return new ModelMealRequestResponse(MealRequestScope.IN_SCOPE, suggestions.suggestions());
    }

    static ModelMealRequestResponse outOfScope() {
        return new ModelMealRequestResponse(MealRequestScope.OUT_OF_SCOPE, List.of());
    }

    boolean isOutOfScope() {
        return scope == MealRequestScope.OUT_OF_SCOPE;
    }

    ModelMealSuggestions inScopeSuggestions() {
        if (isOutOfScope()) {
            throw new IllegalStateException("Out-of-scope requests do not have meal suggestions");
        }

        return new ModelMealSuggestions(suggestions);
    }
}

enum MealRequestScope {
    IN_SCOPE,
    OUT_OF_SCOPE
}

record ModelMealSuggestions(List<ModelMealSuggestion> suggestions) implements Iterable<ModelMealSuggestion> {

    ModelMealSuggestions {
        requireSizeBetween(suggestions, 1, 7, "A response must contain between one and seven suggestions");
        suggestions = List.copyOf(suggestions);
    }

    @Override
    public Iterator<ModelMealSuggestion> iterator() {
        return suggestions.iterator();
    }
}

record ModelMealSuggestion(
        String name,
        int preparationMinutes,
        String explanation,
        int servings,
        List<ModelIngredient> ingredients) {

    ModelMealSuggestion {
        requireNonBlank(name, "A suggestion name is required");
        requireGreaterThanZero(preparationMinutes, "A suggestion needs a positive preparation time");
        requireNonBlank(explanation, "A suggestion explanation is required");
        requireGreaterThanZero(servings, "A suggestion needs positive servings");
        requireNonEmpty(ingredients, "A suggestion requires ingredients");
        ingredients = List.copyOf(ingredients);
    }
}

record ModelIngredient(String productSlug, String quantity, String unit) {

    ModelIngredient {
        requireNonBlank(productSlug, "An ingredient product slug is required");
        requireNonBlank(quantity, "An ingredient quantity is required");
        requireNonBlank(unit, "An ingredient unit is required");
    }
}
