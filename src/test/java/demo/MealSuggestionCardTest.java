package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MealSuggestionCardTest {

    @Test
    void formatsPreparationTimesAsCompactMinutesAndHours() {
        assertThat(MealSuggestionCard.formatPreparationTime(30)).isEqualTo("30 min");
        assertThat(MealSuggestionCard.formatPreparationTime(60)).isEqualTo("1 hr");
        assertThat(MealSuggestionCard.formatPreparationTime(90)).isEqualTo("1 hr 30 min");
    }
}
