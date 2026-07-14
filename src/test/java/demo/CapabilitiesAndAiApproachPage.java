package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class CapabilitiesAndAiApproachPage extends PageObject {

    public CapabilitiesAndAiApproachPage(final Page page) {
        super(page);
    }

    public CapabilitiesAndAiApproachPage shouldExplainCapabilitiesAndAiApproach() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Capabilities and AI approach")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Shape the right use case")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Build trusted AI experiences")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Deliver a working vertical slice")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Prepare for production")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Our AI approach")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Duke Greens in practice")).isVisible();
        assertThat(elementByTextAndExactName("AI understands a meal request and suggests meals. The Java application validates ingredients against its curated catalogue. The visitor reviews the basket and explicitly confirms a simulated order.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Where we can help")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Discuss one business journey with us")).isVisible();
        return this;
    }

    public WelcomePage openDukeGreensDemo() {
        primaryNavigationLink("Demo").click();
        return new WelcomePage(page);
    }
}
