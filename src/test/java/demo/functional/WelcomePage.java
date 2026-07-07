package demo.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public final class WelcomePage {

    private final Page page;

    WelcomePage(final Page page) {
        this.page = page;
    }

    public WelcomePage shouldShowWelcome() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Duke Greens")).isVisible();
        return this;
    }

    public WelcomePage shouldProvideMealRequestInput() {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Describe the meals you want")).isVisible();
        return this;
    }

    private Locator elementByRoleAndExactName(final AriaRole role, final String text) {
        return page.getByRole(role,
                new Page.GetByRoleOptions()
                        .setName(text)
                        .setExact(true));
    }
}
