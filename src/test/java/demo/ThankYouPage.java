package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class ThankYouPage extends PageObject {

    public ThankYouPage(final Page page) {
        super(page);
    }

    public ThankYouPage shouldShowThankYou() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Thank you")).isVisible();
        assertThat(elementByTextAndExactName("Your simulated order is complete. No payment has been taken.")).isVisible();
        return this;
    }

    public WelcomePage returnToWelcomePage() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Return to welcome page").click();
        return new WelcomePage(page);
    }
}
