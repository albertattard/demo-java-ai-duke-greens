package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class DemoNoticeAndResponsibleAiPage extends PageObject {

    public DemoNoticeAndResponsibleAiPage(final Page page) {
        super(page);
    }

    public DemoNoticeAndResponsibleAiPage shouldExplainTheDemonstration() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "About This Demonstration")).isVisible();
        assertThat(elementByTextAndExactName("Duke Greens is a demonstration of an AI-assisted Java application for meal planning and virtual grocery baskets. It is not a production shopping service.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What the demo does")).isVisible();
        assertThat(elementByTextAndExactName("Accepts a meal-planning request.")).isVisible();
        assertThat(elementByTextAndExactName("Uses AI to suggest and explain possible meals.")).isVisible();
        assertThat(elementByTextAndExactName("Validates suggested ingredients against a curated product catalogue.")).isVisible();
        assertThat(elementByTextAndExactName("Creates a review-only virtual basket.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What the demo does not do")).isVisible();
        assertThat(elementByTextAndExactName("It does not accept payments.")).isVisible();
        assertThat(elementByTextAndExactName("It does not create a real order, reserve stock, arrange delivery, or create a customer account.")).isVisible();
        assertThat(elementByTextAndExactName("Product availability, prices, and recommendations are illustrative and may not reflect real-world conditions.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Using AI responsibly")).isVisible();
        assertThat(elementByTextAndExactName("AI is used to interpret requests, recommend meal ideas, and explain alternatives.")).isVisible();
        assertThat(elementByTextAndExactName("The application, not the AI model, remains the source of truth for catalogue products, package sizes, prices, basket contents, and simulated-order completion.")).isVisible();
        assertThat(elementByTextAndExactName("AI output is constrained and checked before it is used in the visitor journey.")).isVisible();
        assertThat(elementByTextAndExactName("The visitor explicitly confirms the simulated order; the application does not complete it autonomously.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Your information")).isVisible();
        assertThat(elementByTextAndExactName("Please do not enter personal, confidential, sensitive, health, payment, or account information.")).isVisible();
        assertThat(elementByTextAndExactName("Keep requests limited to general meal-planning preferences.")).isVisible();
        assertThat(elementByTextAndExactName("Approved privacy notice placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Approved data-retention statement placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Required contact details placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Meal ideas are illustrative and are not medical, dietary, allergy, or nutritional advice.")).isVisible();
        return this;
    }

    public WelcomePage returnToDukeGreensDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Return to the Duke Greens demo").click();
        return new WelcomePage(page);
    }
}
