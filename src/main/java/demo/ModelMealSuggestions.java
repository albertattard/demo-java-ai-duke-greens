package demo;

import static demo.Collections.requireNonEmpty;
import static demo.Collections.requireSizeBetween;
import static demo.Numbers.requireGreaterThanZero;
import static demo.Strings.requireNonBlank;

import module java.base;

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
