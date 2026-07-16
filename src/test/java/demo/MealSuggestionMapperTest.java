package demo;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MealSuggestionMapperTest {

    private final MealSuggestionMapper mapper = new MealSuggestionMapper();
    private final List<Product> catalogue = List.of(
            product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, "g", "1.49"),
            product("passata-700ml", "Passata", 700, "ml", "1.79"),
            product("onions-1kg", "Onions", 1, "kg", "1.29"));

    @Test
    void resolvesAnExactCatalogueSlug() {
        final ModelMealSuggestions modelSuggestions = modelSuggestions(
                modelIngredient("wholewheat-spaghetti-500g", "500", "g"));

        final List<MappedMealSuggestion> mappedSuggestions = mapper.map(modelSuggestions, catalogue);
        final String productName = mappedSuggestions.getFirst().products().getFirst().product().name();

        assertThat(productName)
                .isEqualTo("Wholewheat spaghetti");
    }

    @Test
    void rejectsUnknownOrNonExactCatalogueSlugs() {
        for (final String invalidSlug : List.of("unknown", "Wholewheat-spaghetti-500g", " wholewheat-spaghetti-500g ")) {
            final ModelMealSuggestions modelSuggestions = modelSuggestions(modelIngredient(invalidSlug, "500", "g"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> mapper.map(modelSuggestions, catalogue));
        }
    }

    @Test
    void rejectsDuplicateCatalogueProductSlugs() {
        final ModelMealSuggestions modelSuggestions = modelSuggestions(
                modelIngredient("wholewheat-spaghetti-500g", "500", "g"),
                modelIngredient("wholewheat-spaghetti-500g", "1", "g"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> mapper.map(modelSuggestions, catalogue));
    }

    @Test
    void rejectsMalformedIngredientQuantities() {
        for (final ModelIngredient invalidIngredient : List.of(
                modelIngredient("wholewheat-spaghetti-500g", "1.5", "g"),
                modelIngredient("wholewheat-spaghetti-500g", "0", "g"),
                modelIngredient("wholewheat-spaghetti-500g", "-1", "g"),
                modelIngredient("wholewheat-spaghetti-500g", "100000", "g"),
                modelIngredient("wholewheat-spaghetti-500g", "one", "g"))) {
            final ModelMealSuggestions modelSuggestions = modelSuggestions(invalidIngredient);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> mapper.map(modelSuggestions, catalogue));
        }
    }

    @Test
    void acceptsTheMaximumSupportedIngredientQuantity() {
        final ModelMealSuggestions modelSuggestions = modelSuggestions(modelIngredient("wholewheat-spaghetti-500g", "99999", "g"));
        final List<MappedMealSuggestion> mappedSuggestions = mapper.map(modelSuggestions, catalogue);
        final int packageCount = mappedSuggestions.getFirst().products().getFirst().packageCount();

        assertThat(packageCount)
                .isEqualTo(200);
    }

    @Test
    void rejectsInvalidOrIncompatibleIngredientUnits() {
        for (final ModelIngredient invalidIngredient : List.of(
                modelIngredient("wholewheat-spaghetti-500g", "100", "G"),
                modelIngredient("wholewheat-spaghetti-500g", "100", "ml"))) {
            final ModelMealSuggestions modelSuggestions = modelSuggestions(invalidIngredient);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> mapper.map(modelSuggestions, catalogue));
        }
    }

    @Test
    void roundsCompatibleMassPackagesUpAndCalculatesTheCataloguePrice() {
        final ModelMealSuggestions modelSuggestions = modelSuggestions(
                modelIngredient("wholewheat-spaghetti-500g", "1", "kg"),
                modelIngredient("passata-700ml", "701", "ml"));
        final List<MappedMealSuggestion> mappedSuggestions = mapper.map(modelSuggestions, catalogue);
        final MappedMealSuggestion mappedSuggestion = mappedSuggestions.getFirst();

        assertThat(mappedSuggestion.products())
                .extracting(MappedProduct::packageCount)
                .containsExactly(2, 2);
        assertThat(mappedSuggestion.estimatedCost())
                .isEqualByComparingTo("6.56");
    }

    @Test
    void convertsGramIngredientsAgainstKilogramPackagesBeforeRounding() {
        final ModelMealSuggestions modelSuggestions = modelSuggestions(
                modelIngredient("onions-1kg", "1001", "g"));

        final List<MappedMealSuggestion> mappedSuggestions = mapper.map(modelSuggestions, catalogue);
        final int packageCount = mappedSuggestions.getFirst()
                .products().getFirst().packageCount();

        assertThat(packageCount)
                .isEqualTo(2);
    }

    @Test
    void retainsTheBaseUnitRequirementForConsolidatingSelectedMeals() {
        final List<MappedMealSuggestion> mappedSuggestions = mapper.map(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("First dinner", 20, "A complete dinner.", 1,
                        List.of(modelIngredient("wholewheat-spaghetti-500g", "200", "g"))),
                new ModelMealSuggestion("Second dinner", 20, "Another complete dinner.", 1,
                        List.of(modelIngredient("wholewheat-spaghetti-500g", "300", "g"))))), catalogue);

        assertThat(mappedSuggestions)
                .flatExtracting(MappedMealSuggestion::products)
                .extracting(MappedProduct::requiredQuantity)
                .containsExactly(new BigDecimal("200"), new BigDecimal("300"));
    }

    @Test
    void normalisesSupportedUnitsToTheirDimensionBaseUnit() {
        assertThat(MeasurementUnit.from("g").toBaseUnits(new BigDecimal("250")))
                .isEqualByComparingTo("250");
        assertThat(MeasurementUnit.from("kg").toBaseUnits(new BigDecimal("2")))
                .isEqualByComparingTo("2000");
        assertThat(MeasurementUnit.from("ml").toBaseUnits(new BigDecimal("500")))
                .isEqualByComparingTo("500");

        assertThat(MeasurementUnit.from("g").hasSameDimensionAs(MeasurementUnit.from("kg"))).isTrue();
        assertThat(MeasurementUnit.from("g").hasSameDimensionAs(MeasurementUnit.from("ml"))).isFalse();
        assertThatIllegalArgumentException().isThrownBy(() -> MeasurementUnit.from("l"));
    }

    private static ModelMealSuggestions modelSuggestions(final ModelIngredient... ingredients) {
        return new ModelMealSuggestions(List.of(new ModelMealSuggestion("Dinner", 20, "A complete dinner.", 4, List.of(ingredients))));
    }

    private static ModelIngredient modelIngredient(final String slug, final String quantity, final String unit) {
        return new ModelIngredient(slug, quantity, unit);
    }

    private static Product product(
            final String slug,
            final String name,
            final int packageQuantity,
            final String packageUnit,
            final String price) {
        return new Product(slug, name, packageQuantity, MeasurementUnit.from(packageUnit), new BigDecimal(price));
    }
}
