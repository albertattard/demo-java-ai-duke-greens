package demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import demo.functional.browser.BrowserHarness;
import demo.functional.browser.PlaywrightBrowserHarness;

import module java.base;

@Tag("e2e")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDemoAccess.class)
class WelcomePageIT {

    @LocalServerPort
    private int port;

    @MockitoBean
    private MealSuggestionGenerator mealSuggestionGenerator;

    @Autowired
    private ProductCatalogue productCatalogue;

    private BrowserHarness browser;

    @BeforeAll
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
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
                .shouldPlaceMealRequestActionsOnOneRow()
                .shouldShowProducts(productCatalogue.allProducts().size())
                .shouldShowProduct("Wholewheat spaghetti", "500 g", "1,49")
                .shouldShowProduct("Chickpeas", "400 g", "0,99"));
    }

    @Test
    void takesVisitorsFromPublicInformationToTheProtectedDemo() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openPublicLandingAndStartDemo()
                .shouldShowWelcome()
                .shouldProvideMealRequestInput());
    }

    @Test
    void explainsTheTermsOfUse() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openPublicLandingAndTermsOfUse()
                .shouldExplainTermsOfUse());
    }

    @Test
    void givesTheDemoAccessCodeAndContinueControlsEqualHeight() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openDemoAccessPage()
                .shouldGiveTheAccessControlsEqualHeight());
    }

    @Test
    void makesDemoAccessAndItsAccessRequestProcessClear() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openDemoAccessPage()
                .shouldMakeAccessCodeEntryAndAccessRequestsClear());
    }

    @Test
    void usesOneSharedFrameWidthAcrossPublicProtectedAndInformationPages() throws Exception {
        browser.openDukeGreens(dukeGreens -> {
            dukeGreens.openPublicLandingPage().shouldUseOneSharedFrameWidth();
            dukeGreens.openDemoAccessPage().shouldUseOneSharedFrameWidth();
        });
    }

    @Test
    void explainsTheDemonstrationAndResponsibleAiUseToVisitors() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .openAboutPage()
                .shouldExplainTheDemonstration()
                .returnToDukeGreensDemo()
                .shouldShowWelcome());
    }

    @Test
    void helpsVisitorsRecoverFromAnInvalidLink() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openMissingPage()
                .shouldExplainTheMissingPage()
                .startANewMealPlan()
                .shouldShowWelcome());
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

        when(mealSuggestionGenerator.suggest(request(request)))
                .thenReturn(ModelMealRequestResponse.withSuggestions(suggestions));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldShowSuggestions(3)
                .shouldNotShowProducts()
                .shouldShowPreparationTime("Lemon lentil pasta", "25 min")
                .shouldShowServingCount("Lemon lentil pasta", 1)
                .shouldShowMappedProduct("Red lentils", "500 g", "1,69", 1, "1,69")
                .shouldNotProvideMealRequestInput()
                .reload()
                .shouldShowSuggestions(3)
                .startOverWithoutSelection()
                .shouldShowInitialRequestState());

        verify(mealSuggestionGenerator).suggest(request(request));
    }

    @Test
    void keepsInitialMealIdeasVisibleUntilTheVisitorRefinesThem() throws Exception {
        final String request = "Suggest two quick vegetarian dinners";
        final ModelMealSuggestions initialSuggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Lemon lentil pasta", 25, "A fast vegetarian dinner.", 1, List.of(new ModelIngredient("red-lentils-500g", "100", "g"))),
                new ModelMealSuggestion("Smoky chickpea rice", 25, "A quick plant-based dinner.", 1, List.of(new ModelIngredient("chickpeas-400g", "200", "g")))));
        when(mealSuggestionGenerator.suggest(request(request)))
                .thenReturn(ModelMealRequestResponse.withSuggestions(initialSuggestions));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldShowMealIdeas("Lemon lentil pasta", "Smoky chickpea rice")
                .reload()
                .shouldShowMealIdeas("Lemon lentil pasta", "Smoky chickpea rice"));

        verify(mealSuggestionGenerator).suggest(request(request));
    }

    @Test
    void showsAnAccessibleValidationMessageForABlankMealRequest() throws Exception {
        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitInvalidMealRequest("   ")
                .shouldShowBlankRequestValidation()
                .reload()
                .shouldShowInitialRequestState());

        verifyNoInteractions(mealSuggestionGenerator);
    }

    @Test
    void showsTheRecoveryStateWithoutSuggestionsWhenTheModelReturnsAnUnknownProduct() throws Exception {
        final String request = "Suggest a vegetarian dinner";
        final ModelMealSuggestions unmappableSuggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Unknown product dinner", 20, "A complete meal.", 1,
                        List.of(new ModelIngredient("unknown-product", "100", "g")))));
        when(mealSuggestionGenerator.suggest(request(request)))
                .thenReturn(ModelMealRequestResponse.withSuggestions(unmappableSuggestions));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldOfferRetryAndReset()
                .shouldNotShowMealSuggestions()
                .reload()
                .shouldOfferRetryAndReset());

        verify(mealSuggestionGenerator).suggest(request(request));
    }

    @Test
    void retriesTheSameRequestAfterAModelFailure() throws Exception {
        final String request = "make it fail";
        final ModelMealSuggestions retrySuggestions = new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Retry dinner", 20, "A complete meal after retrying.", 1, List.of(new ModelIngredient("chopped-tomatoes-400g", "1", "g")))));
        when(mealSuggestionGenerator.suggest(request(request)))
                .thenThrow(new IllegalStateException("simulated provider failure"))
                .thenReturn(ModelMealRequestResponse.withSuggestions(retrySuggestions));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .shouldOfferRetryAndReset()
                .retryMealRequest()
                .shouldShowSuggestions(1));

        verify(mealSuggestionGenerator, times(2)).suggest(request(request));
    }

    @Test
    void resetsToTheInitialRequestStateAfterAModelFailure() throws Exception {
        when(mealSuggestionGenerator.suggest(request("make it fail")))
                .thenThrow(new IllegalStateException("simulated provider failure"));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest("make it fail")
                .shouldOfferRetryAndReset()
                .resetMealRequest()
                .shouldShowInitialRequestState());
    }

    @Test
    void consolidatesSelectedMealsOnTheDedicatedBasketPage() throws Exception {
        final String request = "Suggest two pasta dinners";
        when(mealSuggestionGenerator.suggest(request(request))).thenReturn(ModelMealRequestResponse.withSuggestions(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("First pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"))),
                new ModelMealSuggestion("Second pasta", 20, "Another complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "300", "g")))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .shouldShowMealIdeas("Second pasta")
                .shouldShowBasketMeals("First pasta")
                .shouldShowBasketMeal("First pasta", "20 min", 1, "1,49")
                .addMealToBasket(0)
                .shouldShowSelectedMeal(0)
                .shouldShowSelectedMeal(1)
                .shouldShowBasketMeal("Second pasta", "20 min", 1, "1,49")
                .openBasket()
                .shouldShowMealSelection()
                .shouldShowBasketLine("Wholewheat spaghetti", 1, "1,49")
                .clearMeal("First pasta")
                .shouldShowBasketLine("Wholewheat spaghetti", 1, "1,49")
                .clearMeal("Second pasta")
                .shouldShowEmptyBasket()
                .backToRecommendations());
    }

    @Test
    void keepsTheBasketAccessibleAfterAFollowUpReplacesTheDisplayedRecommendations() throws Exception {
        final String request = "Suggest a pasta dinner";
        when(mealSuggestionGenerator.suggest(request(request))).thenReturn(ModelMealRequestResponse.withSuggestions(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g")))))));
        when(mealSuggestionGenerator.suggest(request("Make it quicker"))).thenReturn(ModelMealRequestResponse.withSuggestions(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Quick pasta", 15, "A quicker dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g")))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .followUp("Make it quicker")
                .shouldShowMealIdeas("Quick pasta")
                .shouldPlaceBasketActionsBetweenRecommendationsAndConversation()
                .openBasket()
                .shouldShowBasketLine("Wholewheat spaghetti", 1, "1,49"));
    }

    @Test
    void reviewsTheBasketAtCheckoutAndReturnsToEditingIt() throws Exception {
        final String request = "Suggest a pasta dinner";
        when(mealSuggestionGenerator.suggest(request(request))).thenReturn(ModelMealRequestResponse.withSuggestions(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g")))))));

        browser.openDukeGreens(dukeGreens -> dukeGreens.openWelcomePage()
                .submitMealRequest(request)
                .addMealToBasket(0)
                .openBasket()
                .proceedToCheckout()
                .shouldShowCheckout("1,49")
                .backToBasket()
                .clearMeal("Pasta")
                .shouldShowEmptyBasket());
    }

    @Test
    void completesASimulatedOrderAndReturnsToAFreshWelcomePage() throws Exception {
        final String request = "Suggest a pasta dinner";
        when(mealSuggestionGenerator.suggest(request(request))).thenReturn(ModelMealRequestResponse.withSuggestions(new ModelMealSuggestions(List.of(
                new ModelMealSuggestion("Pasta", 20, "A complete dinner.", 1,
                        List.of(new ModelIngredient("wholewheat-spaghetti-500g", "200", "g")))))));

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

    private static MealSuggestionGenerator.Request request(final String message) {
        return argThat(request -> request != null && request.request().equals(message));
    }
}
