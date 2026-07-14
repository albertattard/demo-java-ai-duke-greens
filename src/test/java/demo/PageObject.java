package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static org.assertj.core.api.Assertions.assertThat;

abstract class PageObject {

    final Page page;

    PageObject(final Page page) {
        this.page = page;
    }

    Locator elementByRoleAndExactName(final AriaRole role, final String text) {
        return page.getByRole(role, new Page.GetByRoleOptions().setName(text).setExact(true));
    }

    Locator elementByTextAndExactName(final String text) {
        return page.getByText(text, new Page.GetByTextOptions().setExact(true));
    }

    Locator primaryNavigationLink(final String text) {
        return page.getByRole(AriaRole.NAVIGATION, new Page.GetByRoleOptions().setName("Duke Greens navigation").setExact(true))
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    public PageObject shouldUseOneSharedFrameWidth() {
        final Number headerWidth = (Number) page.locator(".site-header-content").evaluate("element => element.getBoundingClientRect().width");
        final Number mainWidth = (Number) page.locator("main").evaluate("element => element.getBoundingClientRect().width");
        final Number footerWidth = (Number) page.locator(".site-footer-content").evaluate("element => element.getBoundingClientRect().width");

        assertThat(mainWidth.doubleValue()).isEqualTo(headerWidth.doubleValue());
        assertThat(footerWidth.doubleValue()).isEqualTo(headerWidth.doubleValue());
        return this;
    }

    static Locator elementByRoleAndExactName(final Locator locator, final AriaRole role, final String text) {
        return locator.getByRole(role, new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    static Locator elementByTextAndExactName(final Locator locator, final String text) {
        return locator.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }
}
