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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ProductCatalogue productCatalogue;

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
                .shouldShowProducts(productCatalogue.allProducts().size())
                .shouldShowProduct("Wholewheat spaghetti", "500 g", "1,49")
                .shouldShowProduct("Chickpeas", "400 g", "0,99"));
    }

    @Test
    void highlightsWhyAMissingMealRequestResultReturnedToTheInitialPage() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openRecommendationsWithoutSession()
                .shouldShowNoActiveMealRequest()
                .shouldShowInitialRequestState());

        verifyNoInteractions(mealSuggestionGenerator);
    }

    @Test
    void recommendThreeMealsAsRequested() throws Exception {
        final String request = "Suggest three quick vegetarian dinners for one person";
        final ModelMealSuggestions suggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Lemon lentil pasta", 25, "A fast vegetarian dinner for one.", 1, List.of(new ModelIngredient("red-lentils-500g", "100", "g"))),
                new ModelMealSuggestion("Smoky chickpea rice", 25, "A quick plant-based dinner.", 1, List.of(new ModelIngredient("chickpeas-400g", "200", "g"))),
                new ModelMealSuggestion("Tomato spinach spaghetti", 20, "A simple weeknight meal.", 1, List.of(new ModelIngredient("baby-spinach-200g", "100", "g")))));

        when(mealSuggestionGenerator.suggest(eq(request), anyList()))
                .thenReturn(suggestions);

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldShowSuggestions(3)
                .shouldNotShowProducts()
                .shouldShowMappedProduct("Red lentils", "500 g", "1,69", 1, "1,69")
                .shouldNotProvideMealRequestInput()
                .reload()
                .shouldShowSuggestions(3)
                .startOverWithoutSelection()
                .shouldShowInitialRequestState());

        verify(mealSuggestionGenerator).suggest(eq(request), anyList());
    }

    @Test
    void showsAnAccessibleValidationMessageForABlankMealRequest() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitInvalidMealRequest("   ")
                .shouldShowBlankRequestValidation());

        verifyNoInteractions(mealSuggestionGenerator);
    }

    @Test
    void showsTheRecoveryStateWithoutSuggestionsWhenTheModelReturnsAnUnknownProduct() throws Exception {
        final String request = "Suggest a vegetarian dinner";
        final ModelMealSuggestions unmappableSuggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Unknown product dinner", 20, "A complete meal.", 1,
                        List.of(new ModelIngredient("unknown-product", "100", "g")))));
        when(mealSuggestionGenerator.suggest(eq(request), anyList()))
                .thenReturn(unmappableSuggestions);

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldOfferRetryAndReset()
                .shouldNotShowMealSuggestions()
                .reload()
                .shouldOfferRetryAndReset());

        verify(mealSuggestionGenerator).suggest(eq(request), anyList());
    }

    @Test
    void retriesTheSameRequestAfterAModelFailure() throws Exception {
        final String request = "make it fail";
        final ModelMealSuggestions retrySuggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Retry dinner", 20, "A complete meal after retrying.", 1, List.of(new ModelIngredient("chopped-tomatoes-400g", "1", "g")))));
        when(mealSuggestionGenerator.suggest(eq(request), anyList()))
                .thenThrow(new IllegalStateException("simulated provider failure"))
                .thenReturn(retrySuggestions);

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldOfferRetryAndReset()
                .retryMealRequest()
                .shouldShowSuggestions(1));

        verify(mealSuggestionGenerator, times(2)).suggest(eq(request), anyList());
    }

    @Test
    void resetsToTheInitialRequestStateAfterAModelFailure() throws Exception {
        when(mealSuggestionGenerator.suggest(eq("make it fail"), anyList()))
                .thenThrow(new IllegalStateException("simulated provider failure"));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest("make it fail")
                .shouldOfferRetryAndReset()
                .resetMealRequest()
                .shouldShowInitialRequestState());
    }

    @Test
    void consolidatesSelectedMealsIntoAnEditableBasketWithoutLosingSelection() throws Exception {
        final String request = "Suggest two pasta dinners";
        when(mealSuggestionGenerator.suggest(eq(request), anyList())).thenReturn(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("First pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"))),
                new ModelMealSuggestion("Second pasta", 20, "Another complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "300", "g"))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .addMealToBasket(1)
                .shouldShowSelectedMeal(0)
                .shouldShowSelectedMeal(1)
                .shouldShowBasketLine("Wholewheat spaghetti", 1, "1,49")
                .changeBasketQuantity("Wholewheat spaghetti", 0)
                .shouldShowBasketCoverageWarning()
                .shouldShowSelectedMeal(0)
                .startOver()
                .shouldRequireStartOverConfirmation()
                .confirmStartOver()
                .shouldShowInitialRequestState());
    }

    @Test
    void reviewsTheBasketAtCheckoutAndReturnsToEditingIt() throws Exception {
        final String request = "Suggest a pasta dinner";
        when(mealSuggestionGenerator.suggest(eq(request), anyList())).thenReturn(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .shouldPlaceBasketActionsOnOneRow()
                .proceedToCheckout()
                .shouldShowCheckout("1,49")
                .backToBasket()
                .shouldShowSelectedMeal(0)
                .changeBasketQuantity("Wholewheat spaghetti", 0)
                .shouldShowBasketCoverageWarning());
    }

    @Test
    void completesASimulatedOrderAndReturnsToAFreshWelcomePage() throws Exception {
        final String request = "Suggest a pasta dinner";
        when(mealSuggestionGenerator.suggest(eq(request), anyList())).thenReturn(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .proceedToCheckout()
                .shouldPlaceCheckoutActionsOnOneRow()
                .completeSimulatedOrder()
                .shouldShowThankYou()
                .returnToWelcomePage()
                .shouldShowInitialRequestState());
    }
}
