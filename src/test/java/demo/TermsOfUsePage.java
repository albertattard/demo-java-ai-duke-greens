package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class TermsOfUsePage extends PageObject {

    public TermsOfUsePage(final Page page) {
        super(page);
    }

    public TermsOfUsePage shouldExplainTermsOfUse() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Terms of Use")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Demonstration use only")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "No real shopping service")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Use of AI")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "No warranties")).isVisible();
        return this;
    }
}
