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
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "See the demonstration in action")).isVisible();
        assertThat(elementByTextAndExactName("The best way to understand the pattern is to try it: describe a meal, review the suggestions, and see how the application keeps trusted data and confirmation in your control.")).isVisible();
        return this;
    }

    public WelcomePage returnToDukeGreensDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Try the demo").click();
        return new WelcomePage(page);
    }
}
