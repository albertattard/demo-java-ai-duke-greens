package demo.functional;

import java.util.UUID;

import com.microsoft.playwright.Page;

import demo.CheckoutPage;
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

    public WelcomePage openRecommendationsWithoutSession() {
        page.navigate(baseUrl + "/recommendations/" + UUID.randomUUID());
        return new WelcomePage(page);
    }

    public CheckoutPage openCheckout() {
        page.navigate(baseUrl + "/checkout");
        return new CheckoutPage(page);
    }
}
