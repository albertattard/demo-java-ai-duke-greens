package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class AboutPage extends PageObject {

    public AboutPage(final Page page) {
        super(page);
    }

    public AboutPage shouldExplainTheDemonstration() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "About This Demonstration")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "The business problem")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "How the demo works")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Our AI approach")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What this demo is showing")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What the demo does not do")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Your information")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Try the pattern or discuss your opportunity")).isVisible();
        assertThat(elementByTextAndExactName("Try the demonstration to see how Duke Greens translates a meal request into suggestions while the application keeps trusted data and confirmation in your control.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.LINK, "Discuss an opportunity")).hasAttribute("href", "https://www.oracle.com/java/java-se-subscription/value-engineering/");
        assertThat(elementByRoleAndExactName(AriaRole.LINK, "Discuss an opportunity")).hasAttribute("target", "_blank");
        return this;
    }

    public WelcomePage returnToDukeGreensDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Try the demo").click();
        return new WelcomePage(page);
    }
}
