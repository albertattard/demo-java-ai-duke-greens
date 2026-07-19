package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class FeedbackConfirmationPage extends PageObject {

    public FeedbackConfirmationPage(final Page page) {
        super(page);
    }

    public FeedbackConfirmationPage shouldConfirmFeedbackWasSaved() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Thank you for your feedback")).isVisible();
        assertThat(elementByTextAndExactName("Your feedback has been saved.")).isVisible();
        return this;
    }
}
