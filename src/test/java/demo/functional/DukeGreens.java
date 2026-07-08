package demo.functional;

import com.microsoft.playwright.Page;
import demo.WelcomePage;

public final class DukeGreens {

    private final String baseUrl;
    private final Page page;

    public DukeGreens(final String baseUrl, final Page page) {
        this.baseUrl = baseUrl;
        this.page = page;
    }

    public WelcomePage openWelcomePage() {
        page.navigate(baseUrl);
        return new WelcomePage(page);
    }
}
