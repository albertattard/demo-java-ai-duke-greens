package demo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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

    static Locator elementByRoleAndExactName(final Locator locator, final AriaRole role, final String text) {
        return locator.getByRole(role, new Locator.GetByRoleOptions().setName(text).setExact(true));
    }

    static Locator elementByTextAndExactName(final Locator locator, final String text) {
        return locator.getByText(text, new Locator.GetByTextOptions().setExact(true));
    }
}
