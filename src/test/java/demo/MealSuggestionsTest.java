package demo;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import org.junit.jupiter.api.Test;

import module java.base;

class MealSuggestionsTest {

    @Test
    void rejectsAnEmptySuggestionsList() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MealSuggestions(List.of()));
    }

    @Test
    void rejectsMoreThanSevenSuggestions() {
        final List<MealSuggestion> suggestions = IntStream.range(0, 8)
                .mapToObj(index -> new MealSuggestion("Meal " + index, 20, "A complete meal.", 1, List.of(new Ingredient("Ingredient", "1", "portion"))))
                .toList();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MealSuggestions(suggestions));
    }

    @Test
    void rejectsANonPositivePreparationTime() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MealSuggestion("Meal", 0, "Explanation", 1, List.of(new Ingredient("Tomato", "1", "g"))));
    }

    @Test
    void rejectsABlankIngredientQuantity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Ingredient("Tomato", " ", "g"));
    }
}
