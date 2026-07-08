package demo.functional;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
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

    public WelcomePage shouldShowProducts(int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Products")).isVisible();
        assertThat(page.locator("[data-testid='product-card']")).hasCount(count);
        return this;
    }

    public WelcomePage shouldShowProduct(final String name, final String packageDetail, final String price) {
        final Locator card = page.locator("[data-testid='product-card']")
                .filter(new Locator.FilterOptions().setHasText(name));

        assertThat(card).hasCount(1);
        assertThat(card.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName(name).setExact(true))).isVisible();
        assertThat(card.getByText(packageDetail, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(card.getByText(price + "\u00a0€", new Locator.GetByTextOptions().setExact(true))).isVisible();
        return this;
    }

    private Locator elementByRoleAndExactName(final AriaRole role, final String text) {
        return page.getByRole(role,
                new Page.GetByRoleOptions()
                        .setName(text)
                        .setExact(true));
    }
}
