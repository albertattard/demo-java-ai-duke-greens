package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void rendersProductImagesAndDecorativePlaceholdersOnTheWelcomePage() throws Exception {
        when(productCatalogue.allProducts()).thenReturn(List.of(
                new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49), "pasta-photo.png"),
                new Product("red-peppers-500g", "Red peppers", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(2.49), null)));

        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("/images/300/pasta-photo.png")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("alt=\"Wholewheat spaghetti\"")))
                .andExpect(MockMvcResultMatchers.content().string(not(containsString("alt=\"Red peppers\""))))
                .andExpect(MockMvcResultMatchers.content().string(containsString("aria-hidden=\"true\" class=\"product-image-placeholder\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("Red peppers")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("500 g")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("2,49")));
    }

    @Test
    void rendersAnAccessiblePlaceholderForAMealProductWithoutAnImage() throws Exception {
        final String conversationId = "active-conversation";
        final Product productWithoutImage = new Product("red-peppers-500g", "Red peppers", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(2.49), null);
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Pepper pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(productWithoutImage, 1)), BigDecimal.valueOf(2.49))))));

        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(session))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("class=\"meal-product-image-placeholder\" role=\"img\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("aria-label=\"Product image unavailable\"")));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsAMealRequestSubmissionWithoutACsrfToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/meal-request")
                .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(status().isForbidden());

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnInvalidMealRequestToTheWelcomePageWithItsValidationMessage() throws Exception {
        final String mealRequest = "   ";
        final RequestBuilder request = MockMvcRequestBuilders
                .post("/meal-request")
                .with(csrf())
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrl("/"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/").flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("validationMessage", "Describe at least one meal you want."))
                .andExpect(model().attribute("mealRequest", mealRequest));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void keepsTheActiveConversationWhenANewMealRequestIsInvalid() throws Exception {
        final String conversationId = "active-conversation";
        final SuccessfulMealRequest activeRequest = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", activeRequest);

        mvc.perform(MockMvcRequestBuilders.post("/meal-request").with(csrf()).session(session).param("mealRequest", "   "))
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("mealConversationId", conversationId))
                .andExpect(request().sessionAttribute("mealRequestState", activeRequest));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void clearsThePreviousConversationBeforeStartingANewValidMealRequest() throws Exception {
        final String previousConversationId = "previous-conversation";
        final String mealRequest = "Suggest a vegetarian dinner";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", previousConversationId);
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a meal"));
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        when(mealSuggestionService.submit(anyString(), eq(mealRequest))).thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/meal-request").with(csrf()).session(session).param("mealRequest", mealRequest))
                .andExpect(redirectedUrlPattern("/recommendations/*"));

        final String newConversationId = (String) session.getAttribute("mealConversationId");
        assertThat(newConversationId).isNotEqualTo(previousConversationId);
        verify(mealSuggestionService).clearConversation(previousConversationId);
        verify(mealSuggestionService).submit(eq(newConversationId), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsASuccessfulMealRequestToItsSessionBackedResultPage() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession session = new MockHttpSession();

        when(mealSuggestionService.submit(anyString(), eq(mealRequest)))
                .thenReturn(suggestions);

        final RequestBuilder request = MockMvcRequestBuilders
                .post("/meal-request")
                .with(csrf())
                .session(session).param("mealRequest", mealRequest);

        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrlPattern("/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest(mealRequest, suggestions)))
                .andReturn();

        final String conversationId = (String) session.getAttribute("mealConversationId");
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/recommendations/" + conversationId);

        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attributeExists("suggestions"))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit(eq(conversationId), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAFailedMealRequestToItsSessionBackedRecoveryPage() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final MockHttpSession session = new MockHttpSession();

        when(mealSuggestionService.submit(anyString(), eq(mealRequest)))
                .thenReturn(new FailedRequest(mealRequest));

        final RequestBuilder request = MockMvcRequestBuilders.post("/meal-request")
                .with(csrf())
                .session(session)
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrlPattern("/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new FailedMealRequest(mealRequest)))
                .andReturn();
        final String conversationId = (String) session.getAttribute("mealConversationId");

        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/recommendations/" + conversationId);

        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attribute("failed", true))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit(eq(conversationId), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnOutOfScopeRequestToTheWelcomePageWithoutRetainingActiveState() throws Exception {
        final String mealRequest = "What's the weather?";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner"));

        when(mealSuggestionService.submit(anyString(), eq(mealRequest)))
                .thenReturn(new OutOfScopeRequest(mealRequest));

        final RequestBuilder request = MockMvcRequestBuilders
                .post("/meal-request")
                .with(csrf())
                .session(session)
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist("mealRequestState"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/").session(session).flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two."))
                .andExpect(model().attribute("mealRequest", mealRequest));

        verify(mealSuggestionService).submit(anyString(), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void returnsToTheInitialPageWhenAResultRouteHasNoSessionState() throws Exception {
        final MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/recommendations/no-active-conversation"))
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
        final String conversationId = "active-conversation";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verify(mealSuggestionService).clearConversation(conversationId);
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsToAGetConfirmationPageBeforeClearingASelectedMealRequest() throws Exception {
        final String conversationId = "active-conversation";
        final Product lentils = product("red-lentils-500g");
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        final SuccessfulMealRequest request = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))), Set.of(0), Basket.empty());
        session.setAttribute("mealRequestState", request);

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/recommendations/" + conversationId + "?resetConfirmation=true"))
                .andExpect(request().sessionAttribute("mealRequestState", request));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).param("resetConfirmation", "true").session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attribute("resetConfirmationRequired", true));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void retriesOnlyWhenTheVisitorExplicitlyPostsTheRetainedFailedRequest() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final String conversationId = "active-conversation";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new FailedMealRequest(mealRequest));
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        when(mealSuggestionService.submit(eq(conversationId), eq(mealRequest))).thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/retry").with(csrf()).session(session))
                .andExpect(redirectedUrlPattern("/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest(mealRequest, suggestions)));

        verify(mealSuggestionService).submit(eq(conversationId), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void retriesAFailedRefinementWithItsOriginalConversationAndPendingText() throws Exception {
        final String conversationId = "active-conversation";
        final String refinement = "Make it quicker";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        final SuccessfulMealRequest successfulRequest = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))))
                .prepareRefinement(refinement);
        session.setAttribute("mealRequestState", new FailedRefinementRequest(successfulRequest));
        when(mealSuggestionService.refine(eq(conversationId), eq(refinement), any(), any()))
                .thenReturn(new FailedRequest(refinement));

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/retry").with(csrf()).session(session))
                .andExpect(redirectedUrl("/recommendations/" + conversationId));

        verify(mealSuggestionService).refine(eq(conversationId), eq(refinement), any(), any());
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnOutOfScopeRetryToTheWelcomePageWithoutRetainingActiveState() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final String conversationId = "active-conversation";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new FailedMealRequest(mealRequest));
        when(mealSuggestionService.submit(eq(conversationId), eq(mealRequest))).thenReturn(new OutOfScopeRequest(mealRequest));

        final MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/retry").with(csrf()).session(session))
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist("mealRequestState"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/").session(session).flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two."))
                .andExpect(model().attribute("mealRequest", mealRequest));

        verify(mealSuggestionService).submit(eq(conversationId), eq(mealRequest));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeOneSessionsResultToAnotherSession() throws Exception {
        final String conversationId = "active-conversation";
        final MockHttpSession firstSession = new MockHttpSession();
        firstSession.setAttribute("mealConversationId", conversationId);
        firstSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(new MockHttpSession()))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(firstSession))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeAConversationUrlToAnotherSession() throws Exception {
        final String conversationId = "c2d1c9fd-4dc3-4c95-9a30-4d74d2dcb173";
        final MockHttpSession ownerSession = new MockHttpSession();
        ownerSession.setAttribute("mealConversationId", conversationId);
        ownerSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(ownerSession))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(new MockHttpSession()))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void submitsARefinementOnlyToTheActiveConversationUrl() throws Exception {
        final String conversationId = "c2d1c9fd-4dc3-4c95-9a30-4d74d2dcb173";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));
        when(mealSuggestionService.refine(eq(conversationId), eq("Make it quicker"), any(), any()))
                .thenReturn(new InvalidRequest("Describe how you want to refine the meal ideas."));

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/" + conversationId + "/refine")
                .with(csrf())
                .session(session)
                .param("refinement", "Make it quicker"))
                .andExpect(redirectedUrl("/recommendations/" + conversationId));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/recommendations/" + conversationId + "/refine\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/recommendations/" + conversationId + "/meals\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/recommendations/" + conversationId + "/meals/dismissal\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/recommendations/" + conversationId + "/reset\"")));

        verify(mealSuggestionService).refine(eq(conversationId), eq("Make it quicker"), any(), any());
    }

    @Test
    void rejectsARefinementForAnotherConversationWithoutCallingTheModel() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "active-conversation");
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/other-conversation/refine")
                .with(csrf())
                .session(session)
                .param("refinement", "Make it quicker"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsMealSelectionForAnotherConversationWithoutChangingTheBasket() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final SuccessfulMealRequest request = new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "active-conversation");
        session.setAttribute("mealRequestState", request);

        mvc.perform(MockMvcRequestBuilders.post("/recommendations/other-conversation/meals")
                .with(csrf())
                .session(session)
                .param("index", "0"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"))
                .andExpect(request().sessionAttribute("mealRequestState", request));

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

    @Test
    void completesACompleteBasketAndShowsTheOneTimeThankYouPage() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lentil soup", 25, "A complete dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))),
                Set.of(0), new Basket(Map.of(lentils.slug(), 1))));

        mvc.perform(MockMvcRequestBuilders.post("/checkout/complete").with(csrf()).session(session))
                .andExpect(redirectedUrl("/thank-you"));
        mvc.perform(MockMvcRequestBuilders.get("/thank-you").session(session))
                .andExpect(MockMvcResultMatchers.view().name("thank-you"))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(MockMvcRequestBuilders.get("/recommendations/no-longer-active").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/thank-you").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void refusesToCompleteAnIncompleteBasketOrShowThankYouDirectly() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final Product spaghetti = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lentil soup", 25, "A complete dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69)))),
                Set.of(0), new Basket(Map.of(spaghetti.slug(), 1))));

        mvc.perform(MockMvcRequestBuilders.post("/checkout/complete").with(csrf()).session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/thank-you").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    private static Product product(final String slug) {
        return new Product(slug, "Red lentils", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.69));
    }
}
