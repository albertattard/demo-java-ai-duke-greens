package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class BasketPage extends PageObject {

    public BasketPage(final Page page) {
        super(page);
    }

    public BasketPage shouldShowBasketLine(final String name, final int quantity, final String total) {
        final Locator line = page.locator("[data-testid='basket-line']").filter(new Locator.FilterOptions().setHasText(name));
        assertThat(line).hasCount(1);
        assertThat(elementByTextAndExactName(line, quantity + " pack" + (quantity == 1 ? "" : "s"))).isVisible();
        assertThat(elementByTextAndExactName(line, total + "\u00a0€")).isVisible();
        return this;
    }

    public BasketPage shouldShowMealSelection() {
        assertThat(page.locator(".meal-selection")).hasCSS("border-top-width", "1px");
        assertThat(page.locator(".meal-selection-option").first()).hasCSS("background-color", "rgb(244, 248, 241)");
        assertThat(page.locator(".meal-selection-form")).hasCSS("align-items", "flex-end");
        assertThat(page.locator(".basket-page-actions")).hasCSS("justify-content", "space-between");
        return this;
    }

    public BasketPage clearMeal(final String name) {
        final Locator checkbox = page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName(name).setExact(true));
        checkbox.uncheck();
        elementByRoleAndExactName(AriaRole.BUTTON, "Update basket").click();
        return this;
    }

    public BasketPage shouldShowEmptyBasket() {
        assertThat(elementByTextAndExactName("Your basket is empty. Select meals here or add them from recommendations.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Proceed to checkout")).isHidden();
        return this;
    }

    public CheckoutPage proceedToCheckout() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Proceed to checkout").click();
        return new CheckoutPage(page);
    }

    public RecommendationsPage backToRecommendations() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Back to recommendations").click();
        return new RecommendationsPage(page);
    }
}
