package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import module java.base;

class MealSuggestionSystemMessageFormatterTest {

    private final MealSuggestionSystemMessageFormatter formatter = new MealSuggestionSystemMessageFormatter(
            new ClassPathResource("prompts/meal-suggestion-system.md"));

    @Test
    void rendersTheAuthoritativeCatalogueIntoTheSystemMessage() {
        assertThat(formatter.format(List.of(
                new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49")),
                new Product("tomato-passata-700g", "Tomato passata", 700, MeasurementUnit.GRAM, new BigDecimal("1.79")))))
                .contains("You are Duke Greens' dinner-idea assistant.")
                .contains("- Wholewheat spaghetti (slug: wholewheat-spaghetti-500g)")
                .contains("- Tomato passata (slug: tomato-passata-700g)")
                .doesNotContain("{catalogue}");
    }
}
