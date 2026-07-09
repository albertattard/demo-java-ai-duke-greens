package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("openai-integration")
@SpringBootTest
class OpenAiMealSuggestionIT {

    @Autowired
    private MealSuggestionGenerator generator;

    @Autowired
    private ProductCatalogue productCatalogue;

    @Test
    void returnsOneCompleteStructuredSuggestionFromOpenAi() {
        final ModelMealSuggestions response = generator.suggest("Suggest one quick vegetarian dinner for one person.", productCatalogue.allProducts());

        assertThat(response.suggestions())
                .hasSize(1);
    }
}
