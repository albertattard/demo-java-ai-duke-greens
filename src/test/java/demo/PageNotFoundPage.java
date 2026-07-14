package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class PageNotFoundPage extends PageObject {

    public PageNotFoundPage(final Page page) {
        super(page);
    }

    public PageNotFoundPage shouldExplainTheMissingPage() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "This page has wandered off.")).isVisible();
        assertThat(elementByTextAndExactName("We couldn’t find the page you were looking for. It may have moved, the link may be incomplete, or your demo session may have expired.")).isVisible();
        assertThat(elementByTextAndExactName("Starting again is safe: this is only a demonstration—no real order, payment, or delivery is involved.")).isVisible();
        assertThat(elementByRoleAndExactName(page.locator("main"), AriaRole.LINK, "How Duke Greens creates value")).isVisible();
        return this;
    }

    public WelcomePage startANewMealPlan() {
        elementByRoleAndExactName(AriaRole.LINK, "Start a new meal plan").click();
        return new WelcomePage(page);
    }
}
