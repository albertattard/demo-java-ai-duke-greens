package demo;

import module java.base;

interface MealSuggestionGenerator {

    ModelMealSuggestions suggest(String request, List<Product> catalogue);
}
