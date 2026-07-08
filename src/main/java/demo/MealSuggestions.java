package demo;

import static demo.Strings.requireNonBlank;

import module java.base;

record MealSuggestions(List<MealSuggestion> suggestions)
        implements MealRequestResult, Iterable<MealSuggestion> {

    MealSuggestions {
        if (suggestions == null || suggestions.isEmpty() || suggestions.size() > 7) {
            throw new IllegalArgumentException("A response must contain between one and seven suggestions");
        }
        suggestions = List.copyOf(suggestions);
    }

    int size() {
        return suggestions.size();
    }

    @Override
    public Iterator<MealSuggestion> iterator() {
        return suggestions.iterator();
    }
}

record MealSuggestion(
        String name,
        int preparationMinutes,
        String explanation,
        int servings,
        List<Ingredient> ingredients) {

    MealSuggestion {
        requireNonBlank(name, "A suggestion name is required");
        if (preparationMinutes <= 0) {
            throw new IllegalArgumentException("A suggestion needs a positive preparation time");
        }

        requireNonBlank(explanation, "A suggestion explanation is required");
        if (servings <= 0) {
            throw new IllegalArgumentException("A suggestion needs positive servings");
        }

        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("A suggestion requires ingredients");
        }
        ingredients = List.copyOf(ingredients);
    }
}

record Ingredient(String name, String quantity, String unit) {

    Ingredient {
        requireNonBlank(name, "An ingredient name is required");
        requireNonBlank(quantity, "An ingredient quantity is required");
        requireNonBlank(unit, "An ingredient unit is required");
    }
}
