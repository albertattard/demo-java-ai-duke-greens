package demo.functional;

import java.util.UUID;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import static org.assertj.core.api.Assertions.assertThat;

import demo.CheckoutPage;
import demo.TestDemoAccess;
import demo.PageNotFoundPage;
import demo.TermsOfUsePage;
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
        page.locator("#access-code").fill(TestDemoAccess.accessCode());
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        return new WelcomePage(page);
    }

    public DukeGreens openDemoAccessPage() {
        page.navigate(baseUrl + "/demo-access");
        return this;
    }

    public DukeGreens openPublicLandingPage() {
        page.navigate(baseUrl);
        return this;
    }

    public DukeGreens shouldUseOneSharedFrameWidth() {
        final Number headerWidth = (Number) page.locator(".site-header-content").evaluate("element => element.getBoundingClientRect().width");
        final Number mainWidth = (Number) page.locator("main").evaluate("element => element.getBoundingClientRect().width");
        final Number footerWidth = (Number) page.locator(".site-footer-content").evaluate("element => element.getBoundingClientRect().width");

        assertThat(mainWidth.doubleValue()).isEqualTo(headerWidth.doubleValue());
        assertThat(footerWidth.doubleValue()).isEqualTo(headerWidth.doubleValue());
        return this;
    }

    public DukeGreens shouldGiveTheAccessControlsEqualHeight() {
        final Number accessCodeHeight = (Number) page.locator("#access-code").evaluate("element => element.getBoundingClientRect().height");
        final Number continueHeight = (Number) page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).evaluate("element => element.getBoundingClientRect().height");
        final Number accessCodeWidth = (Number) page.locator("#access-code").evaluate("element => element.getBoundingClientRect().width");
        final Number continueWidth = (Number) page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).evaluate("element => element.getBoundingClientRect().width");

        assertThat(accessCodeHeight.doubleValue()).isEqualTo(continueHeight.doubleValue());
        assertThat(continueWidth.doubleValue()).isLessThan(accessCodeWidth.doubleValue());
        assertThat(accessCodeWidth.doubleValue()).isGreaterThanOrEqualTo(480);
        return this;
    }

    public DukeGreens shouldMakeAccessCodeEntryAndAccessRequestsClear() {
        final var accessCode = page.locator("#access-code");
        PlaywrightAssertions.assertThat(accessCode).hasAttribute("placeholder", "Enter access code");
        PlaywrightAssertions.assertThat(accessCode).hasAttribute("type", "password");

        final var revealAccessCode = page.locator("#toggle-access-code");
        PlaywrightAssertions.assertThat(revealAccessCode).isVisible();
        PlaywrightAssertions.assertThat(revealAccessCode).hasAttribute("aria-label", "Reveal entered code");
        revealAccessCode.click();
        PlaywrightAssertions.assertThat(accessCode).hasAttribute("type", "text");
        PlaywrightAssertions.assertThat(revealAccessCode).hasAttribute("aria-pressed", "true");
        PlaywrightAssertions.assertThat(revealAccessCode).hasAttribute("aria-label", "Hide entered code");
        revealAccessCode.click();
        PlaywrightAssertions.assertThat(accessCode).hasAttribute("type", "password");
        PlaywrightAssertions.assertThat(revealAccessCode).hasAttribute("aria-pressed", "false");

        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING, new Page.GetByRoleOptions().setName("Need access?").setExact(true))).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("albert.attard@oracle.com").setExact(true)))
                .hasAttribute("href", "mailto:albert.attard@oracle.com");
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("jae.hahn@oracle.com").setExact(true)))
                .hasAttribute("href", "mailto:jae.hahn@oracle.com");
        PlaywrightAssertions.assertThat(page.locator(".welcome-panel").getByRole(com.microsoft.playwright.options.AriaRole.LINK, new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Return to Duke Greens").setExact(true))).hasCount(0);
        return this;
    }

    public WelcomePage openPublicLandingAndStartDemo() {
        page.navigate(baseUrl);
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BANNER)).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Duke Greens").setExact(true))).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Home").setExact(true))).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Demo").setExact(true))).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("About").setExact(true))).isVisible();
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.CONTENTINFO)).isVisible();
        shouldLinkToJavaResource("learn.java", "https://learn.java");
        shouldLinkToJavaResource("dev.java", "https://dev.java");
        shouldLinkToJavaResource("inside.java", "https://inside.java");
        shouldLinkToJavaResource("ops.java", "https://ops.java");
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Reach out").setExact(true)))
                .hasAttribute("href", "https://www.oracle.com/java/java-se-subscription/value-engineering/");
        PlaywrightAssertions.assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Reach out").setExact(true)))
                .hasAttribute("target", "_blank");
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Try the demo").setExact(true)).click();
        page.locator("#access-code").fill(TestDemoAccess.accessCode());
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        return new WelcomePage(page);
    }

    public TermsOfUsePage openPublicLandingAndTermsOfUse() {
        page.navigate(baseUrl);
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Terms of Use").setExact(true)).click();
        return new TermsOfUsePage(page);
    }

    public WelcomePage openRecommendationsWithoutSession() {
        openWelcomePage();
        page.navigate(baseUrl + "/demo/recommendations/" + UUID.randomUUID());
        return new WelcomePage(page);
    }

    public PageNotFoundPage openMissingPage() {
        openWelcomePage();
        page.navigate(baseUrl + "/a-link-that-does-not-exist");
        return new PageNotFoundPage(page);
    }

    public CheckoutPage openCheckout() {
        page.navigate(baseUrl + "/demo/checkout");
        return new CheckoutPage(page);
    }

    private void shouldLinkToJavaResource(final String name, final String href) {
        final var link = page.getByRole(com.microsoft.playwright.options.AriaRole.NAVIGATION, new Page.GetByRoleOptions().setName("Java resources").setExact(true))
                .getByRole(com.microsoft.playwright.options.AriaRole.LINK, new com.microsoft.playwright.Locator.GetByRoleOptions().setName(name).setExact(true));
        PlaywrightAssertions.assertThat(link).hasAttribute("href", href);
        PlaywrightAssertions.assertThat(link).hasAttribute("target", "_blank");
    }
}
