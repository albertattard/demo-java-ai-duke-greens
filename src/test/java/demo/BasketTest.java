package demo;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BasketTest {

    @Test
    void consolidatesSharedProductRequirementsIntoTheSmallestWholePackQuantity() {
        final Product spaghetti = product("wholewheat-spaghetti-500g", 500, "1.49");
        final MappedMealSuggestion firstMeal = meal("First dinner", new MappedProduct(spaghetti, new BigDecimal("200"), 1));
        final MappedMealSuggestion secondMeal = meal("Second dinner", new MappedProduct(spaghetti, new BigDecimal("300"), 1));

        final Basket basket = Basket.empty()
                .addRequirements(List.of(firstMeal, secondMeal));

        assertThat(basket.quantityOf(spaghetti.slug())).isEqualTo(1);
        assertThat(basket.fulfils(List.of(firstMeal, secondMeal))).isTrue();
    }

    @Test
    void allowsEditsAndReportsWhenTheyNoLongerFulfilSelectedMeals() {
        final Product spaghetti = product("wholewheat-spaghetti-500g", 500, "1.49");
        final MappedMealSuggestion meal = meal("Dinner", new MappedProduct(spaghetti, new BigDecimal("500"), 1));
        final Basket basket = Basket.empty().addRequirements(List.of(meal));

        final Basket increased = basket.changeQuantity(spaghetti.slug(), 2);
        final Basket removed = increased.changeQuantity(spaghetti.slug(), 0);

        assertThat(increased.totalPrice(List.of(spaghetti))).isEqualByComparingTo("2.98");
        assertThat(removed.quantityOf(spaghetti.slug())).isZero();
        assertThat(removed.quantities()).doesNotContainKey(spaghetti.slug());
        assertThat(removed.fulfils(List.of(meal))).isFalse();
    }

    @Test
    void rejectsInvalidBasketQuantityChanges() {
        final Product spaghetti = product("wholewheat-spaghetti-500g", 500, "1.49");
        final Basket basket = new Basket(Map.of(spaghetti.slug(), 1));

        assertThatIllegalArgumentException().isThrownBy(() -> basket.changeQuantity(spaghetti.slug(), -1));
        assertThatIllegalArgumentException().isThrownBy(() -> basket.changeQuantity("not a slug", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> basket.changeQuantity("other-product", 1));
    }

    @Test
    void retainsSelectedMealsAndDoesNotAddTheSameRecommendationTwice() {
        final Product spaghetti = product("wholewheat-spaghetti-500g", 500, "1.49");
        final MappedMealSuggestion meal = meal("Dinner", new MappedProduct(spaghetti, new BigDecimal("500"), 1));
        final SuccessfulMealRequest request = new SuccessfulMealRequest("Suggest dinner", List.of(meal));

        final SuccessfulMealRequest selectedOnce = request.addMeal(0);
        final SuccessfulMealRequest editedBasket = selectedOnce.selectMeals(Set.of());

        assertThat(editedBasket.selectedMealIndexes()).isEmpty();
        assertThat(editedBasket.addMeal(0).basket().quantityOf(spaghetti.slug())).isOne();
    }

    @Test
    void returnsRemovedMealsToRecommendationsAndRebuildsTheProductBasket() {
        final Product spaghetti = product("wholewheat-spaghetti-500g", 500, "1.49");
        final Product lentils = new Product("red-lentils-500g", "Red lentils", 500, MeasurementUnit.GRAM, new BigDecimal("1.69"));
        final MappedMealSuggestion pasta = meal("Pasta", new MappedProduct(spaghetti, new BigDecimal("500"), 1));
        final MappedMealSuggestion lentilSoup = meal("Lentil soup", new MappedProduct(lentils, new BigDecimal("500"), 1));
        final SuccessfulMealRequest request = new SuccessfulMealRequest("Suggest dinner", List.of(pasta, lentilSoup))
                .addMeal(0)
                .addMeal(1);

        final SuccessfulMealRequest removed = request.removeMeal(0, 0);

        assertThat(removed.selectedMealNames()).containsExactly("Lentil soup");
        assertThat(removed.returned(0, 0)).isTrue();
        assertThat(removed.basket().quantities()).containsOnlyKeys(lentils.slug());
        assertThat(removed.addMeal(0, 0).returned(0, 0)).isFalse();
    }

    private static MappedMealSuggestion meal(final String name, final MappedProduct product) {
        return new MappedMealSuggestion(name, 20, "A complete dinner.", 1, List.of(product), product.product().price());
    }

    private static Product product(final String slug, final int quantity, final String price) {
        return new Product(slug, "Wholewheat spaghetti", quantity, MeasurementUnit.GRAM, new BigDecimal(price));
    }
}
