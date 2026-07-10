package demo;

import module java.base;

interface MealSuggestionGenerator {

    ModelMealRequestResponse suggest(String request, List<Product> catalogue);
}
