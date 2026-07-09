package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import module java.base;

@ExtendWith(MockitoExtension.class)
class MealSuggestionServiceTest {

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
        final String request = "Suggest a meal";
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions modelSuggestions = modelSuggestions("wholewheat-spaghetti-500g", "500", "g");
        final MappedMealSuggestions mappedSuggestions = mappedSuggestions(catalogue);
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(eq(request), same(catalogue)))
                .thenReturn(modelSuggestions);
        when(mapper.map(same(modelSuggestions), same(catalogue)))
                .thenReturn(mappedSuggestions);

        final MealRequestResult result = service.submit(request);

        assertThat(result)
                .isSameAs(mappedSuggestions);
        verify(generator).suggest(eq(request), same(catalogue));
        verify(mapper).map(same(modelSuggestions), same(catalogue));
    }

    @Test
    void turnsProviderFailuresAndUnmappableResponsesIntoOneFriendlyFailureState() {
        final List<Product> catalogue = catalogue();
        final ModelMealSuggestions unmappableSuggestions = modelSuggestions("unknown", "100", "g");
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(eq("Suggest a meal"), same(catalogue)))
                .thenThrow(new IllegalStateException("provider timeout"))
                .thenReturn(unmappableSuggestions);
        when(mapper.map(same(unmappableSuggestions), same(catalogue)))
                .thenThrow(new IllegalArgumentException("unknown product"));

        assertThat(service.submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
        assertThat(service.submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
    }

    @Test
    void rejectsBlankRequestsWithoutCallingTheModel() {
        final MealRequestResult result = service.submit("   ");

        assertThat(result)
            .isInstanceOf(InvalidRequest.class);
        verifyNoInteractions(generator, productCatalogue);
    }

    @Test
    void rejectsAnOverlongRequestWithoutLoadingTheCatalogueOrCallingTheModel() {
        final MealRequestResult result = service.submit("x".repeat(301));

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
        when(generator.suggest(eq(request), same(catalogue))).thenReturn(modelSuggestions);
        when(mapper.map(same(modelSuggestions), same(catalogue))).thenReturn(mappedSuggestions(catalogue));

        assertThat(service.submit(request))
                .isInstanceOf(MappedMealSuggestions.class);
    }

    @Test
    void failsWithoutCallingTheModelWhenTheCatalogueIsEmpty() {
        when(productCatalogue.allProducts()).thenReturn(List.of());

        assertThat(service.submit("Suggest a meal"))
                .isInstanceOf(FailedRequest.class);
        verifyNoInteractions(generator);
    }

    private static List<Product> catalogue() {
        return List.of(
                new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49")));
    }

    private static ModelMealSuggestions modelSuggestions(final String slug, final String quantity, final String unit) {
        return new ModelMealSuggestions(List.of(new ModelMealSuggestion("Meal", 20, "A complete meal.", 1, List.of(new ModelIngredient(slug, quantity, unit)))));
    }

    private static MappedMealSuggestions mappedSuggestions(final List<Product> catalogue) {
        return new MappedMealSuggestions(List.of(new MappedMealSuggestion(
                "Meal", 20, "A complete meal.", 1,
                List.of(new MappedProduct(catalogue.getFirst(), 1)), new BigDecimal("1.49"))));
    }
}
