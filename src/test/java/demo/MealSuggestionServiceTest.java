package demo;

import module java.base;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealSuggestionServiceTest {

    private static final String CONVERSATION_ID = "conversation-1";

    @Mock
    private ProductCatalogue productCatalogue;

    @Mock
    private MealSuggestionGenerator generator;

    @Mock
    private MealSuggestionMapper mapper;

    private MealSuggestionService service;

    @BeforeEach
    void setUp() {
        service = new MealSuggestionService(generator, productCatalogue, mapper);
    }

    @Test
    void passesTheSameCatalogueSnapshotToTheGeneratorAndMapper() {
        final String mealRequest = "Suggest a meal";
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions modelSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "500", "g");
        final List<MappedMealSuggestion> mappedSuggestions = mappedSuggestions(catalogue);
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        final MealSuggestionGenerator.Request generatorRequest = new MealSuggestionGenerator.Request(CONVERSATION_ID, mealRequest, catalogue);
        when(generator.suggest(eq(generatorRequest)))
                .thenReturn(ModelMealRequestResponse.inScope(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue)))
                .thenReturn(mappedSuggestions);

        final MealRequestResult result = service.submit(CONVERSATION_ID, mealRequest);

        assertThat(result)
                .isEqualTo(new SuccessfulMealSuggestions("Here are some meal ideas.", mappedSuggestions));
        verify(generator).suggest(eq(generatorRequest));
        verify(mapper).map(eq(modelSuggestions), same(catalogue));
    }

    @Test
    void turnsProviderFailuresAndUnmappableResponsesIntoOneFriendlyFailureState() {
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions unmappableSuggestions = modelSuggestions("unknown", "100", "g");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest("Suggest a meal", catalogue)))
                .thenThrow(new IllegalStateException("provider timeout"))
                .thenReturn(ModelMealRequestResponse.inScope(unmappableSuggestions));
        when(mapper.map(eq(unmappableSuggestions), same(catalogue)))
                .thenThrow(new IllegalArgumentException("unknown product"));

        assertThat(service.submit(CONVERSATION_ID, "Suggest a meal"))
                .isInstanceOf(FailedRequest.class);

        assertThat(service.submit(CONVERSATION_ID, "Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
    }

    @Test
    void rejectsBlankRequestsWithoutCallingTheModel() {
        final MealRequestResult result = service.submit(CONVERSATION_ID, "   ");

        assertThat(result)
                .isInstanceOf(InvalidRequest.class);
        verifyNoInteractions(generator, productCatalogue);
    }

    @Test
    void rejectsAnOverlongRequestWithoutLoadingTheCatalogueOrCallingTheModel() {
        final MealRequestResult result = service.submit(CONVERSATION_ID, "x".repeat(301));

        assertThat(result)
                .isEqualTo(new InvalidRequest("Describe your meal request in 300 characters or fewer."));
        verifyNoInteractions(generator, productCatalogue);
    }

    @Test
    void acceptsARequestAtTheMaximumLength() {
        final String request = "x".repeat(300);
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions modelSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "500", "g");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest(request, catalogue))).thenReturn(ModelMealRequestResponse.inScope(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue))).thenReturn(mappedSuggestions(catalogue));

        assertThat(service.submit(CONVERSATION_ID, request))
                .isInstanceOf(SuccessfulMealSuggestions.class);
    }

    @Test
    void failsWithoutCallingTheModelWhenTheCatalogueIsEmpty() {
        when(productCatalogue.allProducts()).thenReturn(List.of());

        assertThat(service.submit(CONVERSATION_ID, "Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
        verifyNoInteractions(generator);
    }

    @Test
    void returnsAnExplicitOutOfScopeResultWithoutMappingSuggestions() {
        final String request = "What’s the weather?";
        final List<Product> catalogue = catalogue();
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest(request, catalogue))).thenReturn(ModelMealRequestResponse.outOfScope());

        assertThat(service.submit(CONVERSATION_ID, request)).isEqualTo(new OutOfScopeRequest(request));
        verify(generator).suggest(anyRequest(request, catalogue));
        verifyNoInteractions(mapper);
    }

    @Test
    void handlesAMixedRequestAsOutOfScopeWithoutMappingSuggestions() {
        final String request = "Suggest dinner and tell me a joke";
        final List<Product> catalogue = catalogue();
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest(request, catalogue))).thenReturn(ModelMealRequestResponse.outOfScope());

        assertThat(service.submit(CONVERSATION_ID, request)).isEqualTo(new OutOfScopeRequest(request));
        verifyNoInteractions(mapper);
    }

    @Test
    void returnsSuggestionsAndTheAssistantMessageForASuccessfulFollowUp() {
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions modelSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "500", "g");
        final List<MappedMealSuggestion> mappedSuggestions = mappedSuggestions(catalogue);
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        final MealSuggestionGenerator.Request generatorRequest = new MealSuggestionGenerator.Request(CONVERSATION_ID, "Make it quicker", catalogue);
        when(generator.suggest(eq(generatorRequest)))
                .thenReturn(ModelMealRequestResponse.inScope(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue))).thenReturn(mappedSuggestions);

        assertThat(service.followUp("conversation-1", "Make it quicker"))
                .isEqualTo(new SuccessfulMealSuggestions("Here are some meal ideas.", mappedSuggestions));

        verify(generator).suggest(eq(generatorRequest));
    }

    @Test
    void successfulSuggestionsRequireBetweenOneAndSevenMappedMeals() {
        final MappedMealSuggestion suggestion = mappedSuggestions(catalogue()).getFirst();

        assertThatThrownBy(() -> new SuccessfulMealSuggestions("Here are some meal ideas.", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A response must contain between one and seven suggestions");
        assertThatThrownBy(() -> new SuccessfulMealSuggestions("Here are some meal ideas.", List.of(
                suggestion, suggestion, suggestion, suggestion, suggestion, suggestion, suggestion, suggestion)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A response must contain between one and seven suggestions");
    }

    private static MealSuggestionGenerator.Request anyRequest(final String request, final List<Product> catalogue) {
        return new MealSuggestionGenerator.Request(CONVERSATION_ID, request, catalogue);
    }

    private static List<Product> catalogue() {
        return List.of(
                new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49")));
    }

    private static ModelMealSuggestions modelSuggestions(final String slug, final String quantity, final String unit) {
        return new ModelMealSuggestions(List.of(new ModelMealSuggestion("Meal", 20, "A complete meal.", 1, List.of(new ModelIngredient(slug, quantity, unit)))));
    }

    private static List<MappedMealSuggestion> mappedSuggestions(final List<Product> catalogue) {
        return List.of(new MappedMealSuggestion(
                "Meal", 20, "A complete meal.", 1,
                List.of(new MappedProduct(catalogue.getFirst(), 1)), new BigDecimal("1.49")));
    }
}
