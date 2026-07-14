package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class DemoGuidePage extends PageObject {

    public DemoGuidePage(final Page page) {
        super(page);
    }

    public DemoGuidePage shouldExplainHowDukeGreensCreatesValue() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "How Duke Greens creates value")).isVisible();
        assertThat(elementByTextAndExactName("Duke Greens demonstrates how a Java application can turn a shopper’s natural-language meal-planning request into suitable meal ideas and a review-only virtual grocery basket.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Tell us what you need")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "AI proposes suitable meals")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "The application validates and prepares the basket")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Why this matters")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Trust by design")).isVisible();
        assertThat(elementByTextAndExactName("AI recommends and explains. It is not the source of truth for products, package sizes, prices, basket contents, or completed orders.")).isVisible();
        assertThat(elementByTextAndExactName("The Java application validates recommendations against its own catalogue.")).isVisible();
        assertThat(elementByTextAndExactName("You must explicitly confirm the simulated order.")).isVisible();
        assertThat(elementByTextAndExactName("This demo does not take payment, arrange delivery, or create a real order.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What to try")).isVisible();
        assertThat(elementByTextAndExactName("“I live alone and need three light vegetarian dinners that I can prepare in 25 minutes after work.”")).isVisible();
        return this;
    }

    public WelcomePage tryTheMealPlanningDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Try the meal-planning demo").click();
        return new WelcomePage(page);
    }
}
