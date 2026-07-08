package demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import demo.functional.browser.BrowserHarness;
import demo.functional.browser.PlaywrightBrowserHarness;

import module java.base;

@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WelcomePageIT {

    @LocalServerPort
    private int port;

    @MockitoBean
    private MealSuggestionGenerator mealSuggestionGenerator;

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
    void presentsWelcomeMealRequestInputAndProducts() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .shouldShowWelcome()
                .shouldProvideMealRequestInput()
                .shouldShowProducts(12)
                .shouldShowProduct("Wholewheat spaghetti", "500 g", "1,49")
                .shouldShowProduct("Chickpeas", "400 g", "0,99"));
    }

    @Test
    void recommendThreeMealsAsRequested() throws Exception {
        final String request = "Suggest three quick vegetarian dinners for one person";
        final MealSuggestions suggestions = new MealSuggestions(List.of(
                new MealSuggestion("Lemon lentil pasta", 25, "A fast vegetarian dinner for one.", 1, List.of(new Ingredient("Red lentils", "100", "g"))),
                new MealSuggestion("Smoky chickpea rice", 25, "A quick plant-based dinner.", 1, List.of(new Ingredient("Chickpeas", "200", "g"))),
                new MealSuggestion("Tomato spinach spaghetti", 20, "A simple weeknight meal.", 1, List.of(new Ingredient("Baby spinach", "100", "g")))));

        when(mealSuggestionGenerator.suggest(request))
                .thenReturn(suggestions);

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldShowSuggestions(suggestions)
                .shouldNotProvideMealRequestInput());
    }

    @Test
    void showsAnAccessibleValidationMessageForABlankMealRequest() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest("   ")
                .shouldShowBlankRequestValidation());

        verifyNoInteractions(mealSuggestionGenerator);
    }

    @Test
    void retriesTheSameRequestAfterAModelFailure() throws Exception {
        final String request = "make it fail";
        final MealSuggestions retrySuggestions = new MealSuggestions(List.of(
                new MealSuggestion("Retry dinner", 20, "A complete meal after retrying.", 1, List.of(new Ingredient("Tomato", "1", "whole")))));
        when(mealSuggestionGenerator.suggest(request))
                .thenThrow(new IllegalStateException("simulated provider failure"))
                .thenReturn(retrySuggestions);

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldOfferRetryAndReset()
                .retryMealRequest()
                .shouldShowSuggestions(retrySuggestions));

        verify(mealSuggestionGenerator, times(2)).suggest(request);
    }

    @Test
    void resetsToTheInitialRequestStateAfterAModelFailure() throws Exception {
        when(mealSuggestionGenerator.suggest("make it fail"))
                .thenThrow(new IllegalStateException("simulated provider failure"));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest("make it fail")
                .shouldOfferRetryAndReset()
                .resetMealRequest()
                .shouldShowInitialRequestState());
    }
}
