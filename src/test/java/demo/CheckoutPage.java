package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class CheckoutPage extends PageObject {

    public CheckoutPage(final Page page) {
        super(page);
    }

    public CheckoutPage shouldShowCheckout(final String total) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Checkout")).isVisible();
        assertThat(elementByTextAndExactName("Basket total: " + total + "\u00a0€")).isVisible();
        return this;
    }

    public RecommendationsPage backToBasket() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Back to basket").click();
        return new RecommendationsPage(page);
    }

    public ThankYouPage completeSimulatedOrder() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Complete simulated order").click();
        return new ThankYouPage(page);
    }

    public CheckoutPage shouldPlaceCheckoutActionsOnOneRow() {
        assertThat(page.locator(".checkout-actions")).hasCSS("display", "flex");
        assertThat(page.locator(".checkout-actions")).hasCSS("justify-content", "space-between");
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Back to basket")).hasCSS("background-color", "rgb(255, 255, 255)");
        return this;
    }
}
