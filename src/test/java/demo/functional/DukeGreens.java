package demo.functional;

import java.util.UUID;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

import demo.CheckoutPage;
import demo.TestDemoAccess;
import demo.PageNotFoundPage;
import demo.TeamAndServicesPage;
import demo.WelcomePage;

public final class DukeGreens {

    private final String baseUrl;
    private final Page page;

    public DukeGreens(final String baseUrl, final Page page) {
        this.baseUrl = baseUrl;
        this.page = page;
    }

    public WelcomePage openWelcomePage() {
        page.navigate(baseUrl + "/demo");
        page.getByLabel("Access code").fill(TestDemoAccess.accessCode());
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        return new WelcomePage(page);
    }

    public DukeGreens openDemoAccessPage() {
        page.navigate(baseUrl + "/demo-access");
        return this;
    }

    public DukeGreens shouldGiveTheAccessControlsEqualHeight() {
        final Number accessCodeHeight = (Number) page.getByLabel("Access code").evaluate("element => element.getBoundingClientRect().height");
        final Number continueHeight = (Number) page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).evaluate("element => element.getBoundingClientRect().height");
        final Number accessCodeWidth = (Number) page.getByLabel("Access code").evaluate("element => element.getBoundingClientRect().width");
        final Number continueWidth = (Number) page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).evaluate("element => element.getBoundingClientRect().width");

        assertThat(accessCodeHeight.doubleValue()).isEqualTo(continueHeight.doubleValue());
        assertThat(continueWidth.doubleValue()).isLessThan(accessCodeWidth.doubleValue());
        return this;
    }

    public WelcomePage openPublicLandingAndStartDemo() {
        page.navigate(baseUrl);
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Start the demonstration").setExact(true)).click();
        page.getByLabel("Access code").fill(TestDemoAccess.accessCode());
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        return new WelcomePage(page);
    }

    public TeamAndServicesPage openTeamAndServicesPage() {
        page.navigate(baseUrl + "/team-and-services");
        return new TeamAndServicesPage(page);
    }

    public WelcomePage openRecommendationsWithoutSession() {
        openWelcomePage();
        page.navigate(baseUrl + "/demo/recommendations/" + UUID.randomUUID());
        return new WelcomePage(page);
    }

    public PageNotFoundPage openMissingPage() {
        page.navigate(baseUrl + "/a-link-that-does-not-exist");
        return new PageNotFoundPage(page);
    }

    public CheckoutPage openCheckout() {
        page.navigate(baseUrl + "/demo/checkout");
        return new CheckoutPage(page);
    }
}
