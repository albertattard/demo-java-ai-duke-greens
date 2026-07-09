package demo;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import org.junit.jupiter.api.Test;

import module java.base;

class ModelMealSuggestionsTest {

    @Test
    void rejectsAnEmptySuggestionsList() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestions(List.of()));
    }

    @Test
    void rejectsMoreThanSevenSuggestions() {
        final List<ModelMealSuggestion> suggestions = IntStream.range(0, 8)
                .mapToObj(index -> new ModelMealSuggestion("Meal " + index, 20, "A complete meal.", 1, List.of(new ModelIngredient("Ingredient", "1", "portion"))))
                .toList();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestions(suggestions));
    }

    @Test
    void rejectsANonPositivePreparationTime() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestion("Meal", 0, "Explanation", 1, List.of(new ModelIngredient("Tomato", "1", "g"))));
    }

    @Test
    void rejectsABlankIngredientQuantity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelIngredient("Tomato", " ", "g"));
    }
}
