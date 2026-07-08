package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import module java.base;

class MealSuggestionServiceTest {

    @Test
    void acceptsOneToSevenCompleteSuggestions() {
        final MealSuggestionService service = new MealSuggestionService(request -> suggestions(7));

        final MealRequestResult result = service.submit("Suggest seven dinners");

        assertThat(result)
                .isInstanceOfSatisfying(MealSuggestions.class,
                        suggestions -> assertThat(suggestions.suggestions()).hasSize(7));
    }

    @Test
    void rejectsBlankRequestsWithoutCallingTheModel() {
        final MealSuggestionService service = new MealSuggestionService(request -> {
            throw new AssertionError("The model must not be called");
        });

        final MealRequestResult result = service.submit("   ");

        assertThat(result)
                .isInstanceOfSatisfying(InvalidRequest.class,
                        invalidRequest -> assertThat(invalidRequest.message()).isEqualTo("Describe at least one meal you want."));
    }

    @Test
    void turnsProviderFailuresAndInvalidResponsesIntoOneFriendlyFailureState() {
        assertThat(new MealSuggestionService(request -> { throw new IllegalStateException("provider timeout"); })
                .submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
        assertThat(new MealSuggestionService(request -> new MealSuggestions(List.of()))
                .submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
        assertThat(new MealSuggestionService(request -> suggestions(8))
                .submit("Suggest eight meals"))
                .isInstanceOf(FailedRequest.class);
        assertThat(new MealSuggestionService(request -> new MealSuggestions(List.of(new MealSuggestion("", 20, "Explanation", 1, List.of(new Ingredient("Tomato", "1", "g"))))))
                .submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
    }

    private static MealSuggestions suggestions(final int count) {
        return new MealSuggestions(IntStream.range(0, count)
                .mapToObj(index -> new MealSuggestion("Meal " + index, 20, "A complete meal.", 1, List.of(new Ingredient("Ingredient", "1", "portion"))))
                .toList());
    }
}
