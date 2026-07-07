package demo;

import demo.functional.browser.BrowserHarness;
import demo.functional.browser.PlaywrightBrowserHarness;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WelcomePageIT {

    @LocalServerPort
    private int port;

    private BrowserHarness browser;

    @BeforeAll
    void launchBrowser() {
        browser = new PlaywrightBrowserHarness("http://localhost:" + port);
    }

    @AfterAll
    void closeBrowser() {
        browser.close();
    }

    @Test
    void presentsWelcomeAndMealRequestInput() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .shouldShowWelcome()
                .shouldProvideMealRequestInput());
    }
}
