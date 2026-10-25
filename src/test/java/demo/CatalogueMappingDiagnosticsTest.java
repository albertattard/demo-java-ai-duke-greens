package demo;

import module java.base;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class CatalogueMappingDiagnosticsTest {

    @Test
    void logsUnknownSlugAndItsLocationOnBothAttemptsWithoutChangingTheCorrection(final CapturedOutput output) {
        assertDiagnostics("unlisted-product", false, output);
    }

    @Test
    void logsDuplicateSlugAndItsLocationOnBothAttemptsWithoutChangingTheCorrection(final CapturedOutput output) {
        assertDiagnostics("pasta", true, output);
    }

    @Test
    void escapesAndBoundsModelControlledSlugs() {
        final CatalogueMappingException failure = new CatalogueMappingException(
                "unknown_slug", "bad\r\n\t\u001b\u2028\u2029'\\" + "x".repeat(200), 2, 3);

        assertThat(failure.diagnostic())
                .contains("productSlug='bad\\u000d\\u000a\\u0009\\u001b\\u2028\\u2029\\u0027\\u005c")
                .endsWith("...'")
                .doesNotContain("\r", "\n", "\t", "\u001b", "\u2028", "\u2029", "x".repeat(160));
    }

    private void assertDiagnostics(final String offendingSlug, final boolean duplicate, final CapturedOutput output) {
        final List<Product> catalogue = List.of(new Product("pasta", "Pasta", 500, MeasurementUnit.from("g"), new BigDecimal("1.49")));
        final ModelIngredient validIngredient = new ModelIngredient("pasta", "100", "g");
        final ModelMealRequestResponse response = new ModelMealRequestResponse("Private assistant message", List.of(
                new ModelMealSuggestion("Private first meal", 20, "Private explanation", 2, List.of(validIngredient)),
                new ModelMealSuggestion("Private second meal", 20, "Private explanation", 2,
                        List.of(validIngredient, new ModelIngredient(offendingSlug, "100", "g")))));
        final MealSuggestionGenerator generator = mock(MealSuggestionGenerator.class);
        final ProductCatalogue productCatalogue = mock(ProductCatalogue.class);
        when(productCatalogue.allProducts()).thenReturn(catalogue);
        when(generator.suggest(any())).thenReturn(response);
        final MealSuggestionService service = new MealSuggestionService(generator, productCatalogue, new MealSuggestionMapper());
        final MealSuggestionGenerator.Request initial = new MealSuggestionGenerator.Request("diagnostic-conversation", "Private visitor request", catalogue);

        assertThat(service.submit(new MealSuggestionService.Request(initial.conversationId(), initial.request())))
                .isEqualTo(new FailedRequest(initial.request()));

        verify(generator).suggest(initial);
        verify(generator).suggest(initial.correctionFor("Every ingredient must name one distinct catalogue product"));
        verifyNoMoreInteractions(generator);
        final String diagnostic = "reason=%s mealIndex=2 ingredientIndex=2 productSlug='%s'"
                .formatted(duplicate ? "duplicate_slug" : "unknown_slug", offendingSlug);
        final List<String> warnings = output.getOut().lines()
                .filter(line -> line.contains("meal suggestion response failed catalogue mapping"))
                .toList();
        assertThat(warnings).hasSize(2).allSatisfy(line -> assertThat(line)
                .contains("conversationId=diagnostic-conversation", diagnostic));
        assertThat(warnings.getFirst()).contains("initial", "requesting one correction");
        assertThat(warnings.getLast()).contains("corrected");
        assertThat(output.getAll()).doesNotContain("Private visitor request", "Private assistant message", "Private first meal",
                "Private second meal", "Private explanation");
    }
}
