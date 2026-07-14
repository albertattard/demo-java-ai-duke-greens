package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class TeamAndServicesPage extends PageObject {

    public TeamAndServicesPage(final Page page) {
        super(page);
    }

    public TeamAndServicesPage shouldIntroduceTheTeamAndServices() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Turn AI opportunities into practical Java solutions")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "What we do")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "How we work")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Why Duke Greens")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Meet the team")).isVisible();
        assertThat(elementByTextAndExactName("Duke Greens shows the division of responsibility that makes an AI experience useful and trustworthy. AI understands a customer’s natural-language meal request and proposes ideas. The Java application remains in control of the catalogue, prices, basket contents, and order confirmation.")).isVisible();
        assertThat(page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("Photograph placeholder").setExact(true))).hasCount(3);
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Let’s discuss your opportunity")).isVisible();
        return this;
    }
}
