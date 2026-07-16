package demo;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("openai-integration")
@SpringBootTest
@ContextConfiguration(initializers = TestDemoAccess.class)
class OpenAiMealSuggestionIT {

    @Autowired
    private MealSuggestionGenerator generator;

    @Autowired
    private ProductCatalogue productCatalogue;

    @Test
    void returnOneRecommendation() {
        final ModelMealRequestResponse response = generator.suggest(request("Suggest one quick vegetarian dinner for one person."));

        assertThat(response.scope())
                .describedAs("A request for a vegetarian dinner is within the meal-suggestion scope")
                .isEqualTo(MealRequestScope.IN_SCOPE);

        final ModelMealSuggestions suggestions = response.inScopeSuggestions();
        assertThat(suggestions)
                .describedAs("A request for one dinner receives exactly one recommendation")
                .hasSize(1);

        final ModelMealSuggestion suggestion = suggestions.iterator().next();
        assertThat(suggestion.servings())
                .describedAs("A dinner requested for one person serves one person")
                .isEqualTo(1);
    }

    @Test
    void returnAnExplicitOutOfScopeResponse() {
        final ModelMealRequestResponse response = generator.suggest(request("What’s the weather?"));

        assertThat(response.scope())
                .describedAs("A weather question is outside the meal-suggestion scope")
                .isEqualTo(MealRequestScope.OUT_OF_SCOPE);
        assertThat(response.suggestions())
                .describedAs("An out-of-scope response must not contain meal recommendations")
                .isEmpty();
    }

    @Test
    void refineTheRecommendation() {
        final String conversationId = UUID.randomUUID().toString();
        final ModelMealRequestResponse response1 = generator.suggest(request(conversationId, "Recommend a meal I can cook for four people."));

        assertThat(response1.scope())
                .describedAs("An initial request for a meal is within the meal-suggestion scope")
                .isEqualTo(MealRequestScope.IN_SCOPE);

        final ModelMealSuggestions suggestions1 = response1.inScopeSuggestions();
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

        assertThat(response2.scope())
                .describedAs("A food-preference follow-up remains a valid meal-suggestion request")
                .isEqualTo(MealRequestScope.IN_SCOPE);

        final ModelMealSuggestions suggestions2 = response2.inScopeSuggestions();
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
        return request(UUID.randomUUID().toString(), message);
    }

    private MealSuggestionGenerator.Request request(final String conversationId, final String message) {
        return new MealSuggestionGenerator.Request(conversationId, message, productCatalogue.allProducts(), Set.of(), Set.of());
    }
}
