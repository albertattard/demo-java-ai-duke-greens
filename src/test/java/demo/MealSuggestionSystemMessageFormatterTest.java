package demo;

import module java.base;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class MealSuggestionSystemMessageFormatterTest {

    private static final Product PRODUCT_WHOLEWHEAT_SPAGHETTI = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49"));
    private static final Product PRODUCT_TOMATO_PASSATA = new Product("tomato-passata-700g", "Tomato passata", 700, MeasurementUnit.GRAM, new BigDecimal("1.79"));

    private final MealSuggestionMessageFormatter formatter = new MealSuggestionMessageFormatter(
            new ClassPathResource("prompts/meal-suggestion-system.md"));

    @Test
    void rendersTheAuthoritativeCatalogueIntoTheSystemMessage() {
        final MealSuggestionGenerator.Request request = request("Test", PRODUCT_WHOLEWHEAT_SPAGHETTI, PRODUCT_TOMATO_PASSATA);

        assertThat(formatter.systemMessage(request))
                .contains("You are Duke Greens’ dinner-idea assistant.")
                .contains("“Suggest two quick vegetarian dinners”, “What could I make with lentils?”")
                .contains("treat a follow-up that only adds or changes a meal preference")
                .contains("A request that combines a meal-idea request with any other request is OUT_OF_SCOPE.")
                .contains("- " + PRODUCT_WHOLEWHEAT_SPAGHETTI.name() + " (slug: " + PRODUCT_WHOLEWHEAT_SPAGHETTI.slug() + ")")
                .contains("- " + PRODUCT_TOMATO_PASSATA.name() + " (slug: " + PRODUCT_TOMATO_PASSATA.slug() + ")")
                .doesNotContain("{catalogue}");
    }

    @Test
    void rendersRecommendationAndSelectionContextBeforeTheVisitorFollowUp() {
        final MealSuggestionGenerator.Request request = new MealSuggestionGenerator.Request(
                randomRequestId(),
                "Make it quicker",
                List.of(PRODUCT_WHOLEWHEAT_SPAGHETTI),
                Set.of("Lentil curry"),
                Set.of("Mushroom rice bowl"));

        assertThat(formatter.userMessage(request))
                .containsSubsequence(
                        "The visitor is currently considering the following meal ideas:",
                        "- Lentil curry",
                        "The visitor has selected the following meals (soft positive preference):",
                        "- Mushroom rice bowl",
                        "The visitor’s request:",
                        "Make it quicker");
    }

    @Test
    void rendersOnlyTheVisitorRequestWhenThereAreNoRecommendationsOrSelections() {
        assertThat(formatter.userMessage(request("A simple meal", PRODUCT_WHOLEWHEAT_SPAGHETTI)))
                .isEqualTo("""
                        The visitor’s request:
                        A simple meal""");
    }

    private static MealSuggestionGenerator.Request request(final String request, final Product... catalogue) {
        return new MealSuggestionGenerator.Request(randomRequestId(), request, Arrays.asList(catalogue), Set.of(), Set.of());
    }

    private static String randomRequestId() {
        return UUID.randomUUID().toString();
    }
}
