package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class WelcomePage extends PageObject {

    public WelcomePage(final Page page) {
        super(page);
    }

    public WelcomePage shouldShowWelcome() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Duke Greens")).isVisible();
        return this;
    }

    public TeamAndServicesPage openTeamAndServices() {
        elementByRoleAndExactName(AriaRole.LINK, "Team and services").click();
        return new TeamAndServicesPage(page);
    }

    public CapabilitiesAndAiApproachPage openCapabilitiesAndAiApproach() {
        elementByRoleAndExactName(AriaRole.LINK, "Capabilities and AI approach").click();
        return new CapabilitiesAndAiApproachPage(page);
    }

    public DemoGuidePage openDemoGuide() {
        elementByRoleAndExactName(AriaRole.LINK, "How Duke Greens creates value").click();
        return new DemoGuidePage(page);
    }

    public WelcomePage shouldShowNoActiveMealRequest() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Meal request unavailable")).isVisible();
        final Locator informationMessage = page.locator(".information-message");
        assertThat(informationMessage).isVisible();
        assertThat(informationMessage).hasAttribute("role", "status");
        assertThat(informationMessage.locator("p")).hasText("There is no active meal request to display.");
        return this;
    }

    public WelcomePage shouldProvideMealRequestInput() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasAttribute("maxlength", "300");
        return this;
    }

    public RecommendationsPage submitMealRequest(final String request) {
        fillMealRequest(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return new RecommendationsPage(page);
    }

    public WelcomePage submitInvalidMealRequest(final String request) {
        fillMealRequest(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return this;
    }

    public WelcomePage submitOutOfScopeRequest(final String request) {
        fillMealRequest(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return this;
    }

    public WelcomePage shouldShowOutOfScopeRecovery(final String request) {
        final Locator recovery = page.locator(".information-message").filter(new Locator.FilterOptions().setHasText("Meal ideas only"));
        assertThat(recovery).isVisible();
        assertThat(recovery).hasAttribute("role", "status");
        assertThat(recovery.locator("p")).hasText("Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two.");
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want")).hasValue(request);
        return this;
    }

    public WelcomePage reload() {
        page.reload();
        return this;
    }

    WelcomePage shouldShowBlankRequestValidation() {
        final Locator validationMessage = page.locator(".validation-message");
        assertThat(validationMessage).isVisible();
        assertThat(validationMessage).hasText("Describe at least one meal you want.");
        assertThat(validationMessage).hasAttribute("aria-live", "polite");
        return this;
    }

    WelcomePage shouldShowInitialRequestState() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasValue("");
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas")).isVisible();
        return this;
    }

    public WelcomePage shouldShowProducts(final int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Products")).isVisible();
        assertThat(page.locator("[data-testid='product-card']")).hasCount(count);
        return this;
    }

    public WelcomePage shouldShowProduct(final String name, final String packageDetail, final String price) {
        final Locator card = page.locator("[data-testid='product-card']").filter(new Locator.FilterOptions().setHasText(name));
        assertThat(card).hasCount(1);
        assertThat(elementByRoleAndExactName(card, AriaRole.HEADING, name)).isVisible();
        assertThat(elementByTextAndExactName(card, packageDetail)).isVisible();
        assertThat(elementByTextAndExactName(card, price + "\u00a0€")).isVisible();
        return this;
    }

    private void fillMealRequest(final String request) {
        elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want").fill(request);
    }
}
