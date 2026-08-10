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
                .thenReturn(ModelMealRequestResponse.withSuggestions(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue)))
                .thenReturn(mappedSuggestions);

        final MealRequestResult result = service.submit(new MealSuggestionService.Request(CONVERSATION_ID, mealRequest));

        assertThat(result)
                .isEqualTo(new SuccessfulMealSuggestions("Here are some meal ideas.", mappedSuggestions));
        verify(generator).suggest(eq(generatorRequest));
        verify(mapper).map(eq(modelSuggestions), same(catalogue));
        verifyNoMoreInteractions(generator);
    }

    @Test
    void turnsProviderFailuresIntoTheFriendlyFailureStateWithoutCorrection() {
        final List<Product> catalogue = catalogue();
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest("Suggest a meal", catalogue)))
                .thenThrow(new IllegalStateException("provider timeout"));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, "Suggest a meal")))
                .isInstanceOf(FailedRequest.class);
        verify(generator).suggest(anyRequest("Suggest a meal", catalogue));
        verifyNoInteractions(mapper);
    }

    @Test
    void correctsAnUnknownProductSlugUsingTheOriginalRequestAndCatalogueSnapshot() {
        final String mealRequest = "Suggest a meal";
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions invalidSuggestions = modelSuggestions("unknown", "100", "g");
        final ModelMealSuggestions correctedSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "100", "g");
        final List<MappedMealSuggestion> mappedSuggestions = mappedSuggestions(catalogue);
        final MealSuggestionGenerator.Request initialRequest = anyRequest(mealRequest, catalogue);
        final MealSuggestionGenerator.Request correctionRequest = initialRequest
                .correctionFor("Every ingredient must name one distinct catalogue product");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(initialRequest))
                .thenReturn(ModelMealRequestResponse.withSuggestions(invalidSuggestions));
        when(generator.suggest(correctionRequest))
                .thenReturn(ModelMealRequestResponse.withSuggestions(correctedSuggestions));
        when(mapper.map(invalidSuggestions, catalogue))
                .thenThrow(new IllegalArgumentException("Every ingredient must name one distinct catalogue product"));
        when(mapper.map(correctedSuggestions, catalogue)).thenReturn(mappedSuggestions);

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, mealRequest)))
                .isEqualTo(new SuccessfulMealSuggestions("Here are some meal ideas.", mappedSuggestions));

        verify(generator).suggest(initialRequest);
        verify(generator).suggest(correctionRequest);
        verify(mapper).map(invalidSuggestions, catalogue);
        verify(mapper).map(correctedSuggestions, catalogue);
        verifyNoMoreInteractions(generator, mapper);
    }

    @Test
    void correctsAnInvalidQuantityOnce() {
        final String mealRequest = "Suggest a meal";
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions invalidSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "0", "g");
        final ModelMealSuggestions correctedSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "100", "g");
        final MealSuggestionGenerator.Request initialRequest = anyRequest(mealRequest, catalogue);
        final MealSuggestionGenerator.Request correctionRequest = initialRequest
                .correctionFor("Ingredient quantities must be positive whole integers between 1 and 99999 (five 9s)");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(initialRequest)).thenReturn(ModelMealRequestResponse.withSuggestions(invalidSuggestions));
        when(generator.suggest(correctionRequest)).thenReturn(ModelMealRequestResponse.withSuggestions(correctedSuggestions));
        when(mapper.map(invalidSuggestions, catalogue)).thenThrow(new IllegalArgumentException(correctionRequest.correctionConstraint()));
        when(mapper.map(correctedSuggestions, catalogue)).thenReturn(mappedSuggestions(catalogue));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, mealRequest)))
                .isInstanceOf(SuccessfulMealSuggestions.class);

        verify(generator).suggest(initialRequest);
        verify(generator).suggest(correctionRequest);
        verifyNoMoreInteractions(generator);
    }

    @Test
    void returnsTheSafeFailureStateWhenTheCorrectionAlsoCannotBeMapped() {
        final String mealRequest = "Suggest a meal";
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions invalidSuggestions = modelSuggestions("unknown", "100", "g");
        final MealSuggestionGenerator.Request initialRequest = anyRequest(mealRequest, catalogue);
        final MealSuggestionGenerator.Request correctionRequest = initialRequest
                .correctionFor("Every ingredient must name one distinct catalogue product");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(initialRequest)).thenReturn(ModelMealRequestResponse.withSuggestions(invalidSuggestions));
        when(generator.suggest(correctionRequest)).thenReturn(ModelMealRequestResponse.withSuggestions(invalidSuggestions));
        when(mapper.map(invalidSuggestions, catalogue))
                .thenThrow(new IllegalArgumentException("Every ingredient must name one distinct catalogue product"));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, mealRequest)))
                .isEqualTo(new FailedRequest(mealRequest));

        verify(generator).suggest(initialRequest);
        verify(generator).suggest(correctionRequest);
        verifyNoMoreInteractions(generator);
    }

    @Test
    void rejectsBlankRequestsWithoutCallingTheModel() {
        final MealRequestResult result = service.submit(new MealSuggestionService.Request(CONVERSATION_ID, "   "));

        assertThat(result)
                .isEqualTo(new InvalidRequest("Describe at least one meal you want."));
        verifyNoInteractions(generator, productCatalogue);
    }

    @Test
    void rejectsAnOverlongRequestWithoutLoadingTheCatalogueOrCallingTheModel() {
        final MealRequestResult result = service.submit(new MealSuggestionService.Request(CONVERSATION_ID, "x".repeat(1_001)));

        assertThat(result)
                .isEqualTo(new InvalidRequest("Describe your meal request in 1000 characters or fewer."));
        verifyNoInteractions(generator, productCatalogue);
    }

    @Test
    void acceptsARequestAtTheMaximumLength() {
        final String request = "x".repeat(1_000);
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions modelSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "500", "g");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest(request, catalogue))).thenReturn(ModelMealRequestResponse.withSuggestions(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue))).thenReturn(mappedSuggestions(catalogue));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, request)))
                .isInstanceOf(SuccessfulMealSuggestions.class);
    }

    @Test
    void failsWithoutCallingTheModelWhenTheCatalogueIsEmpty() {
        when(productCatalogue.allProducts()).thenReturn(List.of());

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, "Suggest a meal")))
                .isInstanceOf(FailedRequest.class);
        verifyNoInteractions(generator);
    }

    @Test
    void returnsTheModelGuidanceWithoutMappingSuggestions() {
        final String request = "What’s the weather?";
        final List<Product> catalogue = catalogue();
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(anyRequest(request, catalogue)))
                .thenReturn(new ModelMealRequestResponse("I can help with meal ideas. What would you like to cook?", List.of()));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, request)))
                .isEqualTo(new SuccessfulMealSuggestions("I can help with meal ideas. What would you like to cook?", List.of()));
        verify(generator).suggest(anyRequest(request, catalogue));
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
                .thenReturn(ModelMealRequestResponse.withSuggestions(modelSuggestions));
        when(mapper.map(eq(modelSuggestions), same(catalogue))).thenReturn(mappedSuggestions);

        assertThat(service.submit(new MealSuggestionService.Request("conversation-1", "Make it quicker")))
                .isEqualTo(new SuccessfulMealSuggestions("Here are some meal ideas.", mappedSuggestions));

        verify(generator).suggest(eq(generatorRequest));
    }

    @Test
    void returnsAnAssistantMessageWithoutSuggestionsForAnInformationalFollowUp() {
        final List<Product> catalogue = catalogue();
        final MealSuggestionGenerator.Request generatorRequest = new MealSuggestionGenerator.Request(
                CONVERSATION_ID, "What do you need to know?", catalogue);
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(eq(generatorRequest)))
                .thenReturn(new ModelMealRequestResponse("Do you have any dietary requirements?", List.of()));

        assertThat(service.submit(new MealSuggestionService.Request(CONVERSATION_ID, "What do you need to know?")))
                .isEqualTo(new SuccessfulMealSuggestions("Do you have any dietary requirements?", List.of()));

        verify(generator).suggest(eq(generatorRequest));
        verifyNoInteractions(mapper);
    }

    @Test
    void successfulSuggestionsAllowUpToSevenMappedMeals() {
        final MappedMealSuggestion suggestion = mappedSuggestions(catalogue()).getFirst();

        assertThatThrownBy(() -> new SuccessfulMealSuggestions("Here are some meal ideas.", List.of(
                suggestion, suggestion, suggestion, suggestion, suggestion, suggestion, suggestion, suggestion)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A response must contain between zero and seven suggestions");
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
