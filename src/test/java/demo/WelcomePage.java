package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class WelcomePage {

    private final Page page;

    public WelcomePage(final Page page) {
        this.page = page;
    }

    public WelcomePage shouldShowWelcome() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Duke Greens")).isVisible();
        return this;
    }

    public WelcomePage shouldProvideMealRequestInput() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasAttribute("maxlength", "300");
        return this;
    }

    public WelcomePage submitMealRequest(final String request) {
        elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want").fill(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return this;
    }

    WelcomePage shouldShowBlankRequestValidation() {
        final Locator validationMessage = page.locator(".validation-message");
        assertThat(validationMessage).isVisible();
        assertThat(validationMessage).hasText("Describe at least one meal you want.");
        assertThat(validationMessage).hasAttribute("aria-live", "polite");
        return this;
    }

    public WelcomePage shouldShowSuggestions(final int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Meal ideas")).isVisible();
        assertThat(page.locator("[data-testid='meal-suggestion']")).hasCount(count);
        return this;
    }

    WelcomePage shouldShowMappedProduct(
            final String name,
            final String packageDetail,
            final String price,
            final int packageCount,
            final String estimatedCost) {
        final Locator suggestion = page.locator("[data-testid='meal-suggestion']").filter(new Locator.FilterOptions().setHasText(name));
        assertThat(elementByTextAndExactName(suggestion, packageCount + " × " + name + " (" + packageDetail + "), " + price + " €")).isVisible();
        assertThat(elementByTextAndExactName(suggestion, "Estimated standalone meal cost: " + estimatedCost + " €")).isVisible();
        return this;
    }

    public WelcomePage shouldOfferRetryAndReset() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "We could not create meal ideas")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Try again")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.LINK, "Reset")).isVisible();
        shouldNotProvideMealRequestInput();
        return this;
    }

    public WelcomePage retryMealRequest() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Try again").click();
        return this;
    }

    public WelcomePage resetMealRequest() {
        elementByRoleAndExactName(AriaRole.LINK, "Reset").click();
        return this;
    }

    WelcomePage shouldShowInitialRequestState() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasValue("");
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "We could not create meal ideas")).isHidden();
        return this;
    }

    WelcomePage shouldNotProvideMealRequestInput() {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want")).isHidden();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas")).isHidden();
        return this;
    }

    WelcomePage shouldNotShowMealSuggestions() {
        assertThat(page.locator("[data-testid='meal-suggestion']")).hasCount(0);
        return this;
    }

    public WelcomePage shouldShowProducts(int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Products")).isVisible();
        assertThat(page.locator("[data-testid='product-card']")).hasCount(count);
        return this;
    }

    public WelcomePage shouldShowProduct(final String name, final String packageDetail, final String price) {
        final Locator card = page.locator("[data-testid='product-card']")
                .filter(new Locator.FilterOptions().setHasText(name));

        assertThat(card).hasCount(1);
        assertThat(elementByRoleAndExactName(card, AriaRole.HEADING, name)).isVisible();
        assertThat(elementByTextAndExactName(card, packageDetail)).isVisible();
        assertThat(elementByTextAndExactName(card, price + "\u00a0€")).isVisible();
        return this;
    }

    private Locator elementByRoleAndExactName(final AriaRole role, final String text) {
        return page.getByRole(role, new Page.GetByRoleOptions().setName(text).setExact(true));
    }

    private static Locator elementByRoleAndExactName(final Locator locator, final AriaRole role, final String text) {
        return locator.getByRole(role, new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    private static Locator elementByTextAndExactName(final Locator locator, final String text) {
        return locator.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }
}
