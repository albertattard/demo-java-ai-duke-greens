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
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What the demo does")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What the demo does not do")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Using AI responsibly")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Your information")).isVisible();
        return this;
    }

    public WelcomePage returnToDukeGreensDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Try the demo").click();
        return new WelcomePage(page);
    }
}
