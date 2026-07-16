package demo;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ModelMealSuggestionsTest {

    @Test
    void representsAnOutOfScopeResponseWithAnEmptyProviderSuggestionsList() {
        final ModelMealRequestResponse response = ModelMealRequestResponse.outOfScope();

        assertThat(response.scope()).isEqualTo(MealRequestScope.OUT_OF_SCOPE);
        assertThat(response.suggestions()).isEmpty();
    }

    @Test
    void rejectsAnInScopeResponseWithNoSuggestions() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealRequestResponse(MealRequestScope.IN_SCOPE, "A response", List.of()));
    }

    @Test
    void rejectsAnEmptySuggestionsList() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestions(List.of()));
    }

    @Test
    void rejectsMoreThanSevenSuggestions() {
        final List<ModelMealSuggestion> suggestions = IntStream.range(0, 8)
                .mapToObj(index -> new ModelMealSuggestion(
                        "Meal " + index,
                        20,
                        "A complete meal.",
                        1,
                        List.of(new ModelIngredient("Ingredient", "1", "portion"))))
                .toList();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestions(suggestions));
    }

    @Test
    void rejectsANonPositivePreparationTime() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelMealSuggestion(
                        "Meal",
                        0,
                        "Explanation",
                        1,
                        List.of(new ModelIngredient("Tomato", "1", "g"))));
    }

    @Test
    void rejectsABlankIngredientQuantity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ModelIngredient("Tomato", " ", "g"));
    }
}
