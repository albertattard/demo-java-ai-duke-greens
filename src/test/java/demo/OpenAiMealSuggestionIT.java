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
        final ModelMealRequestResponse response = generator.suggest("Suggest one quick vegetarian dinner for one person.", productCatalogue.allProducts());

        assertThat(response.scope()).isEqualTo(MealRequestScope.IN_SCOPE);
        assertThat(response.inScopeSuggestions())
                .hasSize(1);
    }

    @Test
    void returnsAnExplicitOutOfScopeResponseFromOpenAi() {
        final ModelMealRequestResponse response = generator.suggest("What’s the weather?", productCatalogue.allProducts());

        assertThat(response.scope()).isEqualTo(MealRequestScope.OUT_OF_SCOPE);
        assertThat(response.suggestions()).isEmpty();
    }
}
