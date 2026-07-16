package demo;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Tag("openai-integration")
@SpringBootTest
@ContextConfiguration(initializers = TestDemoAccess.class)
class OpenAiMealSuggestionIT {

    @Autowired
    private MealSuggestionGenerator generator;

    @Autowired
    private ProductCatalogue productCatalogue;

    @Autowired
    private MealSuggestionMapper mapper;

    @Test
    void returnOneRecommendation() {
        final ModelMealRequestResponse response = generator.suggest(request("Suggest one quick vegetarian dinner for one person."));

        final ModelMealSuggestions suggestions = new ModelMealSuggestions(response.suggestions());
        assertThat(suggestions)
                .describedAs("A request for one dinner receives exactly one recommendation")
                .hasSize(1);

        final ModelMealSuggestion suggestion = suggestions.iterator().next();
        assertThat(suggestion.servings())
                .describedAs("A dinner requested for one person serves one person")
                .isEqualTo(1);
        assertThatCode(() -> mapper.map(suggestions, productCatalogue.allProducts()))
                .describedAs("A specified meal request uses only exact application catalogue products")
                .doesNotThrowAnyException();
    }

    @Test
    void guidesAnUnrelatedRequestBackToMealPreparation() {
        final ModelMealRequestResponse response = generator.suggest(request("What’s the weather?"));

        assertThat(response.suggestions())
                .describedAs("An unrelated request does not produce meal recommendations")
                .isEmpty();
        assertThat(response.assistantMessage())
                .describedAs("An unrelated request still receives visitor-facing guidance")
                .isNotBlank();
    }

    @Test
    void refineTheRecommendation() {
        final String conversationId = createConversationId();
        final ModelMealRequestResponse response1 = generator.suggest(request(conversationId, "Recommend one vegetarian meal I can cook in 30 minutes for four people."));

        final ModelMealSuggestions suggestions1 = new ModelMealSuggestions(response1.suggestions());
        assertThat(suggestions1)
                .describedAs("A request for one meal produces exactly one recommendation")
                .hasSize(1);

        final ModelMealSuggestion suggestion1 = suggestions1.iterator().next();
        assertThat(suggestion1.servings())
                .describedAs("The initial recommendation serves the requested four people")
                .isEqualTo(4);

        // Ask the model to recommend something different. Determine whether
        // chicken was recommended, then ask the model to recommend chicken if
        // it was not recommended, or pork if chicken was in the first
        // recommendation.
        final boolean wasChickenRecommended = suggestion1.ingredients().stream()
                .map(ModelIngredient::productSlug)
                .anyMatch("chicken-breast-500g"::equals);
        final String requestFor = wasChickenRecommended
                ? "Pork"
                : "Chicken";
        final String expectedProductSlug = wasChickenRecommended
                ? "pork-mince-500g"
                : "chicken-breast-500g";

        final ModelMealRequestResponse response2 = generator.suggest(request(conversationId, "They like " + requestFor));

        final ModelMealSuggestions suggestions2 = new ModelMealSuggestions(response2.suggestions());
        assertThat(suggestions2)
                .describedAs("A preference-only follow-up keeps the single-recommendation response shape")
                .hasSize(1);

        final ModelMealSuggestion suggestion2 = suggestions2.iterator().next();
        assertThat(suggestion2.servings())
                .describedAs("A preference-only follow-up retains the initial request’s serving count")
                .isEqualTo(4);
        assertThat(suggestion2.ingredients())
                .describedAs("The follow-up recommendation reflects the visitor’s stated meat preference")
                .extracting(ModelIngredient::productSlug)
                .contains(expectedProductSlug);
    }

    private MealSuggestionGenerator.Request request(final String message) {
        return request(createConversationId(), message);
    }

    private MealSuggestionGenerator.Request request(final String conversationId, final String message) {
        return new MealSuggestionGenerator.Request(conversationId, message, productCatalogue.allProducts(), Set.of(), Set.of());
    }

    private static String createConversationId() {
        return UUID.randomUUID().toString();
    }
}
