package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class RecommendationsPage extends PageObject {

    public RecommendationsPage(final Page page) {
        super(page);
    }

    public RecommendationsPage shouldShowSuggestions(final int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Meal ideas")).isVisible();
        assertThat(page.locator("[data-testid='meal-suggestion']")).hasCount(count);
        return this;
    }

    RecommendationsPage shouldShowMappedProduct(
            final String name,
            final String packageDetail,
            final String price,
            final int packageCount,
            final String estimatedCost) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']")
                .filter(new Locator.FilterOptions().setHasText(name));
        final Locator productCard = suggestion.locator("[data-testid='meal-product-card']")
                .filter(new Locator.FilterOptions().setHasText(name));
        assertThat(productCard).hasCount(1);
        assertThat(elementByTextAndExactName(productCard, packageCount + (packageCount == 1 ? " pack" : " packs"))).isVisible();
        assertThat(elementByTextAndExactName(productCard, packageDetail)).isVisible();
        assertThat(elementByTextAndExactName(productCard, price + "\u00A0€")).isVisible();
        assertThat(suggestion.locator(".meal-estimated-cost > span:not(.visually-hidden)")).hasText(estimatedCost + "\u00A0€");
        return this;
    }

    public RecommendationsPage shouldOfferRetryAndReset() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "We could not create meal ideas")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Try again")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Reset")).isVisible();
        return this;
    }

    public RecommendationsPage retryMealRequest() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Try again").click();
        return this;
    }

    public WelcomePage resetMealRequest() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Reset").click();
        return new WelcomePage(page);
    }

    public RecommendationsPage addMealToBasket(final int index) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']").nth(index);
        elementByRoleAndExactName(suggestion, AriaRole.BUTTON, "Add meal to basket").click();
        return this;
    }

    public RecommendationsPage showAlternativeMealIdeas() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Show more ideas").click();
        return this;
    }

    public RecommendationsPage shouldShowMealIdeas(final String... names) {
        assertThat(page.locator("[data-testid='meal-suggestion'] h3")).hasText(names);
        return this;
    }

    RecommendationsPage shouldShowPreparationTime(final String mealName, final String preparationTime) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']")
                .filter(new Locator.FilterOptions().setHasText(mealName));
        assertThat(elementByTextAndExactName(suggestion, preparationTime)).isVisible();
        return this;
    }

    RecommendationsPage shouldShowServingCount(final String mealName, final int servings) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']")
                .filter(new Locator.FilterOptions().setHasText(mealName));
        assertThat(elementByTextAndExactName(suggestion, String.valueOf(servings))).isVisible();
        return this;
    }

    public RecommendationsPage shouldNotOfferAlternativeMealIdeas() {
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Show more ideas")).isHidden();
        return this;
    }

    public RecommendationsPage shouldShowSelectedMeal(final int index) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']").nth(index);
        assertThat(elementByRoleAndExactName(suggestion, AriaRole.LINK, "View basket")).isVisible();
        assertThat(elementByRoleAndExactName(suggestion, AriaRole.BUTTON, "Add meal to basket")).isHidden();
        assertThat(suggestion.locator(".meal-product-grid")).hasCount(0);
        return this;
    }

    public RecommendationsPage shouldShowBasketLine(final String name, final int quantity, final String total) {
        final Locator line = page.locator("[data-testid='basket-line']")
                .filter(new Locator.FilterOptions().setHasText(name));
        assertThat(line).hasCount(1);
        assertThat(elementByRoleAndExactName(line, AriaRole.SPINBUTTON, "Packs")).hasValue(String.valueOf(quantity));
        assertThat(elementByTextAndExactName(line, "— " + total + "\u00A0€")).isVisible();
        return this;
    }

    public RecommendationsPage changeBasketQuantity(final String productName, final int quantity) {
        final Locator line = page.locator("[data-testid='basket-line']")
                .filter(new Locator.FilterOptions().setHasText(productName));
        elementByRoleAndExactName(line, AriaRole.SPINBUTTON, "Packs").fill(String.valueOf(quantity));
        elementByRoleAndExactName(line, AriaRole.BUTTON, "Update basket").click();
        return this;
    }

    public RecommendationsPage shouldShowBasketCoverageWarning() {
        assertThat(elementByTextAndExactName("Your basket no longer contains enough products for all selected meals. You can still continue editing it.")).isVisible();
        return this;
    }

    public CheckoutPage proceedToCheckout() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Proceed to checkout").click();
        return new CheckoutPage(page);
    }

    public BasketPage openBasket() {
        elementByRoleAndExactName(AriaRole.LINK, "View basket").first().click();
        return new BasketPage(page);
    }

    public RecommendationsPage shouldPlaceBasketActionsOnOneRow() {
        assertThat(page.locator(".basket-actions")).hasCSS("display", "flex");
        assertThat(page.locator(".basket-actions")).hasCSS("justify-content", "space-between");
        assertThat(page.locator(".basket-actions")).hasCSS("margin-top", "32px");
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Start over")).hasCSS("background-color", "rgb(255, 255, 255)");
        return this;
    }

    public RecommendationsPage startOver() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Start over").click();
        return this;
    }

    public WelcomePage startOverWithoutSelection() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Start over").click();
        return new WelcomePage(page);
    }

    public RecommendationsPage shouldRequireStartOverConfirmation() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Start over?")).isVisible();
        return this;
    }

    public WelcomePage confirmStartOver() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Clear and start over").click();
        return new WelcomePage(page);
    }

    public RecommendationsPage keepShopping() {
        final Locator confirmationActions = page.locator(".reset-confirmation-actions");
        assertThat(confirmationActions).hasCSS("justify-content", "space-between");
        assertThat(elementByRoleAndExactName(confirmationActions, AriaRole.BUTTON, "Keep shopping")).isVisible();
        assertThat(elementByRoleAndExactName(confirmationActions, AriaRole.BUTTON, "Clear and start over")).isVisible();
        elementByRoleAndExactName(confirmationActions, AriaRole.BUTTON, "Keep shopping").click();
        return this;
    }

    public RecommendationsPage reload() {
        page.reload();
        return this;
    }

    RecommendationsPage shouldNotShowMealSuggestions() {
        assertThat(page.locator("[data-testid='meal-suggestion']")).hasCount(0);
        return this;
    }

    RecommendationsPage shouldNotShowProducts() {
        assertThat(page.locator("[data-testid='product-card']")).hasCount(0);
        return this;
    }

    RecommendationsPage shouldNotProvideMealRequestInput() {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want")).isHidden();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas")).isHidden();
        return this;
    }
}
