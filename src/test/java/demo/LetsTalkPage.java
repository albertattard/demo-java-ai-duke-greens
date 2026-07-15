package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class LetsTalkPage extends PageObject {

    public LetsTalkPage(final Page page) {
        super(page);
    }

    public LetsTalkPage shouldInviteCustomersToTalk() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Let’s explore your next AI opportunity.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Good starting points")).isVisible();
        assertThat(elementByTextAndExactName("Reduce effort in a repetitive customer or employee journey.")).isVisible();
        assertThat(elementByTextAndExactName("Help people find, understand, or act on trusted business information.")).isVisible();
        assertThat(elementByTextAndExactName("Improve the quality or speed of a decision while retaining human oversight.")).isVisible();
        assertThat(elementByTextAndExactName("Validate an AI use case through a small, end-to-end proof of value.")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What happens next")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Bring these to the conversation")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Ways to get in touch")).isVisible();
        assertThat(elementByTextAndExactName("Team email address placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Named contact placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Calendar link placeholder")).isVisible();
        assertThat(elementByTextAndExactName("Internal collaboration channel placeholder")).isVisible();
        return this;
    }

    public WelcomePage openDukeGreensDemo() {
        elementByRoleAndExactName(AriaRole.LINK, "Return to the Duke Greens demo").click();
        return new WelcomePage(page);
    }

}
