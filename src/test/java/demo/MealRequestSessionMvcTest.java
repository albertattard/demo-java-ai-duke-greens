package demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import module java.base;

@WebMvcTest({WelcomePageController.class, RecommendationsPageController.class, CheckoutController.class})
@Import({SecurityConfiguration.class, MealRequestSession.class, BasketPresentation.class})
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class MealRequestSessionMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductCatalogue productCatalogue;

    @MockitoBean
    private MealSuggestionService mealSuggestionService;

    @Test
    void rejectsAMealRequestSubmissionWithoutACsrfToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/meal-request")
                        .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(status().isForbidden());

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsASuccessfulMealRequestToItsSessionBackedResultPage() throws Exception {
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession session = new MockHttpSession();
        when(mealSuggestionService.submit("Suggest a vegetarian dinner"))
                .thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/meal-request").with(csrf()).session(session).param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(redirectedUrl("/recommendations"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", suggestions)));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations").session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attributeExists("suggestions"))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit("Suggest a vegetarian dinner");
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAFailedMealRequestToItsSessionBackedRecoveryPage() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        when(mealSuggestionService.submit("Suggest a vegetarian dinner"))
                .thenReturn(new FailedRequest("Suggest a vegetarian dinner"));

        mvc.perform(MockMvcRequestBuilders.post("/meal-request").with(csrf()).session(session).param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(redirectedUrl("/recommendations"))
                .andExpect(request().sessionAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner")));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations").session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attribute("failed", true))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit("Suggest a vegetarian dinner");
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void returnsToTheInitialPageWhenAResultRouteHasNoSessionState() throws Exception {
        final MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/recommendations"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
        mvc.perform(MockMvcRequestBuilders.get("/").param("notice", "no-active-meal-request"))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("informationMessage", "There is no active meal request to display."));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void clearsSessionStateOnReset() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void retriesOnlyWhenTheVisitorExplicitlyPostsTheRetainedFailedRequest() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner"));
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion( "Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        when(mealSuggestionService.submit("Suggest a vegetarian dinner")).thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/retry").with(csrf()).session(session))
                .andExpect(redirectedUrl("/recommendations"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", suggestions)));

        verify(mealSuggestionService).submit("Suggest a vegetarian dinner");
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeOneSessionsResultToAnotherSession() throws Exception {
        final MockHttpSession firstSession = new MockHttpSession();
        firstSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/recommendations").session(new MockHttpSession()))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations").session(firstSession))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void showsAnAuthoritativeBasketReviewForAPopulatedSuccessfulRequest() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final Product spaghetti = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest two dinners",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))),
                Set.of(0), new Basket(Map.of(lentils.slug(), 2, spaghetti.slug(), 1))));
        when(productCatalogue.allProducts()).thenReturn(List.of(lentils, spaghetti));

        mvc.perform(MockMvcRequestBuilders.get("/checkout").session(session))
                .andExpect(MockMvcResultMatchers.view().name("checkout"))
                .andExpect(model().attribute("basketLines", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(model().attribute("basketTotal", "4,87\u00a0€"))
                .andExpect(model().attribute("basketFulfilsSelectedMeals", true))
                .andExpect(header().string("Cache-Control", "no-store"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void identifiesAnIncompleteBasketAtCheckout() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final Product spaghetti = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))),
                Set.of(0), new Basket(Map.of(spaghetti.slug(), 1))));
        when(productCatalogue.allProducts()).thenReturn(List.of(lentils, spaghetti));

        mvc.perform(MockMvcRequestBuilders.get("/checkout").session(session))
                .andExpect(MockMvcResultMatchers.view().name("checkout"))
                .andExpect(model().attribute("basketFulfilsSelectedMeals", false));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void returnsToTheInitialPageWhenCheckoutHasNoUsableBasketState() throws Exception {
        final MockHttpSession failedSession = new MockHttpSession();
        failedSession.setAttribute("mealRequestState", new FailedMealRequest("Suggest a dinner"));
        final MockHttpSession emptyBasketSession = new MockHttpSession();
        emptyBasketSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/checkout"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/checkout").session(failedSession))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/checkout").session(emptyBasketSession))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    private static Product product(final String slug) {
        return new Product(slug, "Red lentils", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.69));
    }
}
