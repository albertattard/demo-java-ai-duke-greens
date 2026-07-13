package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import module java.base;

class MealSuggestionSystemMessageFormatterTest {

    private final MealSuggestionMessageFormatter formatter = new MealSuggestionMessageFormatter(
            new ClassPathResource("prompts/meal-suggestion-system.md"));

    @Test
    void rendersTheAuthoritativeCatalogueIntoTheSystemMessage() {
        final MealSuggestionGenerator.Request request = request(List.of(
                new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49")),
                new Product("tomato-passata-700g", "Tomato passata", 700, MeasurementUnit.GRAM, new BigDecimal("1.79"))));
        assertThat(formatter.systemMessage(request))
                .contains("You are Duke Greens’ dinner-idea assistant.")
                .contains("“Suggest two quick vegetarian dinners”, “What could I make with lentils?”")
                .contains("treat a follow-up that only adds or changes a meal preference")
                .contains("A request that combines a meal-idea request with any other request is OUT_OF_SCOPE.")
                .contains("- Wholewheat spaghetti (slug: wholewheat-spaghetti-500g)")
                .contains("- Tomato passata (slug: tomato-passata-700g)")
                .doesNotContain("{catalogue}");
    }

    @Test
    void rendersRefinementPreferencesIntoTheUserMessage() {
        final MealSuggestionGenerator.Request request = new MealSuggestionGenerator.Request(
                UUID.randomUUID().toString(),
                "Make it quicker",
                List.of(new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49"))),
                Set.of("Lentil curry"),
                Set.of("Mushroom rice bowl"));

        assertThat(formatter.userMessage(request))
                .contains("- Lentil curry")
                .contains("- Mushroom rice bowl")
                .endsWith("Make it quicker");
    }

    private static MealSuggestionGenerator.Request request(final List<Product> catalogue) {
        return new MealSuggestionGenerator.Request(UUID.randomUUID().toString(), "Test", catalogue, Set.of(), Set.of());
    }
}
