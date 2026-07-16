package demo;

import module java.base;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({WelcomePageController.class, RecommendationsPageController.class, BasketPageController.class, CheckoutController.class})
@Import({SecurityConfiguration.class, MealRequestSession.class, BasketPresentation.class, MealRequestSessionMvcTest.AuthenticatedVisitorConfiguration.class})
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(initializers = TestDemoAccess.class)
class MealRequestSessionMvcTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class AuthenticatedVisitorConfiguration {

        @Bean
        MockMvcBuilderCustomizer authenticatedVisitor() {
            return builder -> builder.defaultRequest(MockMvcRequestBuilders.get("/").with(user("demo-visitor").authorities(() -> "DEMO_VISITOR")));
        }
    }

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

        mvc.perform(MockMvcRequestBuilders.get("/demo"))
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
                List.of(new MappedMealSuggestion("Pepper pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(productWithoutImage, 1)), BigDecimal.valueOf(2.49)))));

        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("class=\"meal-product-image-placeholder\" role=\"img\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("aria-label=\"Product image unavailable\"")));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsAMealRequestSubmissionWithoutACsrfToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/demo/meal-request")
                        .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(status().isForbidden());

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnInvalidMealRequestToTheWelcomePageWithItsValidationMessage() throws Exception {
        final String mealRequest = "   ";
        final String errorMessage = "Describe at least one meal you want.";
        final MockHttpSession session = new MockHttpSession();
        when(mealSuggestionService.submit(any(MealSuggestionService.Request.class)))
                .thenReturn(new InvalidRequest(errorMessage));

        final RequestBuilder request = MockMvcRequestBuilders
                .post("/demo/meal-request")
                .with(csrf())
                .session(session)
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrl("/demo"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/demo").flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("validationMessage", errorMessage))
                .andExpect(model().attribute("mealRequest", mealRequest));

        assertThat(session.getAttribute("mealConversationId")).isNull();
        assertThat(session.getAttribute("mealRequestState")).isNull();
        verify(mealSuggestionService).submit(any(MealSuggestionService.Request.class));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnActiveSessionFromTheWelcomePageToItsRecommendations() throws Exception {
        final String conversationId = "active-conversation";
        final SuccessfulMealRequest activeRequest = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", activeRequest);

        mvc.perform(MockMvcRequestBuilders.get("/demo").session(session))
                .andExpect(redirectedUrl("/demo/recommendations/" + conversationId))
                .andExpect(request().sessionAttribute("mealConversationId", conversationId))
                .andExpect(request().sessionAttribute("mealRequestState", activeRequest));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void clearsAnActiveConversationBeforeHandlingAPostToTheMealRequestEndpoint() throws Exception {
        final String existingConversationId = "active-conversation";
        final String mealRequest = "Suggest a vegetarian dinner";
        final MockHttpSession session = new MockHttpSession();
        final FailedMealRequest activeRequest = new FailedMealRequest("Suggest a meal");
        session.setAttribute("mealConversationId", existingConversationId);
        session.setAttribute("mealRequestState", activeRequest);
        final List<MappedMealSuggestion> suggestions = List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)));
        when(mealSuggestionService.submit(any(MealSuggestionService.Request.class)))
                .thenReturn(new SuccessfulMealSuggestions("Here are some meal ideas.", suggestions));

        mvc.perform(MockMvcRequestBuilders.post("/demo/meal-request").with(csrf()).session(session).param("mealRequest", mealRequest))
                .andExpect(redirectedUrlPattern("/demo/recommendations/*"));

        final String newConversationId = (String) session.getAttribute("mealConversationId");
        assertThat(newConversationId).isNotEqualTo(existingConversationId);
        verify(mealSuggestionService).clearConversation(existingConversationId);
        verify(mealSuggestionService).submit(eq(new MealSuggestionService.Request(newConversationId, mealRequest)));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsASuccessfulMealRequestToItsSessionBackedResultPage() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final List<MappedMealSuggestion> suggestions = List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)));
        final MockHttpSession session = new MockHttpSession();

        when(mealSuggestionService.submit(any(MealSuggestionService.Request.class)))
                .thenReturn(new SuccessfulMealSuggestions("Here are some meal ideas.", suggestions));

        final RequestBuilder request = MockMvcRequestBuilders
                .post("/demo/meal-request")
                .with(csrf())
                .session(session).param("mealRequest", mealRequest);

        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrlPattern("/demo/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest(mealRequest, suggestions)))
                .andReturn();

        final String conversationId = (String) session.getAttribute("mealConversationId");
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/demo/recommendations/" + conversationId);

        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attributeExists("recommendations"))
                .andExpect(model().attributeExists("basketMeals"))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAFailedMealRequestToItsSessionBackedRecoveryPage() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final MockHttpSession session = new MockHttpSession();

        when(mealSuggestionService.submit(any(MealSuggestionService.Request.class)))
                .thenReturn(new FailedRequest(mealRequest));

        final RequestBuilder request = MockMvcRequestBuilders.post("/demo/meal-request")
                .with(csrf())
                .session(session)
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrlPattern("/demo/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new FailedMealRequest(mealRequest)))
                .andReturn();
        final String conversationId = (String) session.getAttribute("mealConversationId");

        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/demo/recommendations/" + conversationId);

        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"))
                .andExpect(model().attribute("failed", true))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnOutOfScopeRequestToTheWelcomePageWithoutRetainingActiveState() throws Exception {
        final String mealRequest = "What's the weather?";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner"));

        when(mealSuggestionService.submit(any(MealSuggestionService.Request.class)))
                .thenReturn(new OutOfScopeRequest(mealRequest));

        final RequestBuilder request = MockMvcRequestBuilders
                .post("/demo/meal-request")
                .with(csrf())
                .session(session)
                .param("mealRequest", mealRequest);
        final MvcResult result = mvc.perform(request)
                .andExpect(redirectedUrl("/demo"))
                .andExpect(request().sessionAttributeDoesNotExist("mealRequestState"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/demo").session(session).flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two."))
                .andExpect(model().attribute("mealRequest", mealRequest));

        final ArgumentCaptor<MealSuggestionService.Request> submittedRequest = ArgumentCaptor.forClass(MealSuggestionService.Request.class);
        verify(mealSuggestionService).submit(submittedRequest.capture());
        assertThat(submittedRequest.getValue().conversationId()).isNotBlank();
        assertThat(submittedRequest.getValue().request()).isEqualTo(mealRequest);
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void returnsToTheInitialPageWhenAResultRouteHasNoSessionState() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/no-active-conversation"))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"))
                .andExpect(request().sessionAttributeDoesNotExist("mealRequestState"));

        mvc.perform(MockMvcRequestBuilders.get("/demo").param("notice", "no-active-meal-request"))
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
                List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/demo"));
        mvc.perform(MockMvcRequestBuilders.get("/demo").session(session))
                .andExpect(MockMvcResultMatchers.view().name("welcome"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

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
                List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))), Set.of(0), Basket.empty());
        session.setAttribute("mealRequestState", request);

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/demo/recommendations/" + conversationId + "?resetConfirmation=true"))
                .andExpect(request().sessionAttribute("mealRequestState", request));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).param("resetConfirmation", "true").session(session))
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
        final List<MappedMealSuggestion> suggestions = List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)));
        when(mealSuggestionService.submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)))).thenReturn(new SuccessfulMealSuggestions("Here are some meal ideas.", suggestions));

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/retry").with(csrf()).session(session))
                .andExpect(redirectedUrlPattern("/demo/recommendations/*"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest(mealRequest, suggestions)));

        verify(mealSuggestionService).submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void redirectsAnOutOfScopeRetryToTheWelcomePageWithoutRetainingActiveState() throws Exception {
        final String mealRequest = "Suggest a vegetarian dinner";
        final String conversationId = "active-conversation";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new FailedMealRequest(mealRequest));
        when(mealSuggestionService.submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)))).thenReturn(new OutOfScopeRequest(mealRequest));

        final MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/retry").with(csrf()).session(session))
                .andExpect(redirectedUrl("/demo"))
                .andExpect(request().sessionAttributeDoesNotExist("mealRequestState"))
                .andReturn();
        mvc.perform(MockMvcRequestBuilders.get("/demo").session(session).flashAttrs(result.getFlashMap()))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two."))
                .andExpect(model().attribute("mealRequest", mealRequest));

        verify(mealSuggestionService).submit(eq(new MealSuggestionService.Request(conversationId, mealRequest)));
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeOneSessionsResultToAnotherSession() throws Exception {
        final String conversationId = "active-conversation";
        final MockHttpSession firstSession = new MockHttpSession();
        firstSession.setAttribute("mealConversationId", conversationId);
        firstSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));

        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(new MockHttpSession()))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(firstSession))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeAConversationUrlToAnotherSession() throws Exception {
        final String conversationId = "c2d1c9fd-4dc3-4c95-9a30-4d74d2dcb173";
        final MockHttpSession ownerSession = new MockHttpSession();
        ownerSession.setAttribute("mealConversationId", conversationId);
        ownerSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));

        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(ownerSession))
                .andExpect(MockMvcResultMatchers.view().name("recommendations"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(new MockHttpSession()))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void submitsAFollowUpOnlyToTheActiveConversationUrl() throws Exception {
        final String conversationId = "c2d1c9fd-4dc3-4c95-9a30-4d74d2dcb173";
        final String mealRequest = "Make it quicker";
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));
        final MealSuggestionService.Request followUpRequest = new MealSuggestionService.Request(
                conversationId,
                mealRequest,
                Set.of("Lemon lentil pasta"),
                Set.of());
        when(mealSuggestionService.submit(eq(followUpRequest)))
                .thenReturn(new InvalidRequest("Describe what you’d like to change about the meal ideas."));

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/follow-up")
                        .with(csrf())
                        .session(session)
                        .param("followUp", mealRequest))
                .andExpect(redirectedUrl("/demo/recommendations/" + conversationId));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/demo/recommendations/" + conversationId + "/follow-up\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/demo/recommendations/" + conversationId + "/meals\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("action=\"/demo/recommendations/" + conversationId + "/reset\"")));

        verify(mealSuggestionService).submit(eq(followUpRequest));
    }

    @Test
    void keepsTheBasketAccessibleAfterASuccessfulFollowUp() throws Exception {
        final String conversationId = "active-conversation";
        final String mealRequest = "Make it quicker";
        final Product lentils = product("red-lentils-500g");
        final MappedMealSuggestion initialSuggestion = new MappedMealSuggestion("Lemon lentil pasta", 25,
                "A quick dinner.", 1, List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69));
        final MappedMealSuggestion alternativeSuggestion = new MappedMealSuggestion("Lentil curry", 30,
                "A hearty dinner.", 1, List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69));
        final MappedMealSuggestion followUpSuggestion = new MappedMealSuggestion("Lentil soup", 20,
                "A quicker dinner.", 1, List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", conversationId);
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner",
                List.of(initialSuggestion, alternativeSuggestion)).addMeal(0));
        when(productCatalogue.allProducts()).thenReturn(List.of(lentils));
        final MealSuggestionService.Request followUpRequest = new MealSuggestionService.Request(
                conversationId,
                mealRequest,
                Set.of(initialSuggestion.name(), alternativeSuggestion.name()),
                Set.of(initialSuggestion.name()));
        when(mealSuggestionService.submit(eq(followUpRequest)))
                .thenReturn(new SuccessfulMealSuggestions("Here is a quicker idea.", List.of(followUpSuggestion)));

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/" + conversationId + "/follow-up")
                        .with(csrf())
                        .session(session)
                        .param("followUp", mealRequest))
                .andExpect(redirectedUrl("/demo/recommendations/" + conversationId));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/" + conversationId).session(session))
                .andExpect(MockMvcResultMatchers.content().string(containsString("href=\"/demo/basket/" + conversationId + "\"")));

        verify(mealSuggestionService).submit(eq(followUpRequest));
    }

    @Test
    void rejectsAFollowUpForAnotherConversationWithoutCallingTheModel() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "active-conversation");
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/other-conversation/follow-up")
                        .with(csrf())
                        .session(session)
                        .param("followUp", "Make it quicker"))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsMealSelectionForAnotherConversationWithoutChangingTheBasket() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final SuccessfulMealRequest request = new SuccessfulMealRequest("Suggest a vegetarian dinner", List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "active-conversation");
        session.setAttribute("mealRequestState", request);

        mvc.perform(MockMvcRequestBuilders.post("/demo/recommendations/other-conversation/meals")
                        .with(csrf())
                        .session(session)
                        .param("index", "0"))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"))
                .andExpect(request().sessionAttribute("mealRequestState", request));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsMalformedAndStaleBasketPostsWithoutChangingTheActiveConversation() throws Exception {
        final String activeConversationId = "active-conversation";
        final SuccessfulMealRequest activeRequest = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", activeConversationId);
        session.setAttribute("mealRequestState", activeRequest);

        mvc.perform(MockMvcRequestBuilders.post("/demo/basket/not-a-uuid")
                        .with(csrf())
                        .session(session)
                        .param("meal", "0:0"))
                .andExpect(redirectedUrl("/demo/recommendations/" + activeConversationId + "?notice=basket-unavailable"))
                .andExpect(request().sessionAttribute("mealRequestState", activeRequest));
        mvc.perform(MockMvcRequestBuilders.post("/demo/basket/stale-conversation")
                        .with(csrf())
                        .session(session)
                        .param("meal", "0:0"))
                .andExpect(redirectedUrl("/demo/recommendations/" + activeConversationId + "?notice=basket-unavailable"))
                .andExpect(request().sessionAttribute("mealRequestState", activeRequest));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsAnotherSessionsBasketPostWithoutChangingThatSessionsState() throws Exception {
        final String otherConversationId = "other-conversation";
        final SuccessfulMealRequest otherRequest = new SuccessfulMealRequest("Suggest a vegetarian dinner",
                List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        final MockHttpSession otherSession = new MockHttpSession();
        otherSession.setAttribute("mealConversationId", otherConversationId);
        otherSession.setAttribute("mealRequestState", otherRequest);

        mvc.perform(MockMvcRequestBuilders.post("/demo/basket/owners-conversation")
                        .with(csrf())
                        .session(otherSession)
                        .param("meal", "0:0"))
                .andExpect(redirectedUrl("/demo/recommendations/" + otherConversationId + "?notice=basket-unavailable"))
                .andExpect(request().sessionAttribute("mealRequestState", otherRequest));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void rejectsBasketPostsWithoutAnActiveConversation() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/demo/basket/stale-conversation")
                        .with(csrf())
                        .param("meal", "0:0"))
                .andExpect(redirectedUrl("/demo?notice=basket-unavailable"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void showsAnAuthoritativeBasketReviewForAPopulatedSuccessfulRequest() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final Product spaghetti = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest two dinners",
                List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))),
                Set.of(0), new Basket(Map.of(lentils.slug(), 2, spaghetti.slug(), 1))));
        when(productCatalogue.allProducts()).thenReturn(List.of(lentils, spaghetti));

        mvc.perform(MockMvcRequestBuilders.get("/demo/checkout").session(session))
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
                List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))),
                Set.of(0), new Basket(Map.of(spaghetti.slug(), 1))));
        when(productCatalogue.allProducts()).thenReturn(List.of(lentils, spaghetti));

        mvc.perform(MockMvcRequestBuilders.get("/demo/checkout").session(session))
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
                List.of(new MappedMealSuggestion("Lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69)))));

        mvc.perform(MockMvcRequestBuilders.get("/demo/checkout"))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/checkout").session(failedSession))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/checkout").session(emptyBasketSession))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void completesACompleteBasketAndShowsTheOneTimeThankYouPage() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                List.of(new MappedMealSuggestion("Lentil soup", 25, "A complete dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))),
                Set.of(0), new Basket(Map.of(lentils.slug(), 1))));

        mvc.perform(MockMvcRequestBuilders.post("/demo/checkout/complete").with(csrf()).session(session))
                .andExpect(redirectedUrl("/demo/thank-you"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/thank-you").session(session))
                .andExpect(MockMvcResultMatchers.view().name("thank-you"))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/recommendations/no-longer-active").session(session))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/thank-you").session(session))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void refusesToCompleteAnIncompleteBasketOrShowThankYouDirectly() throws Exception {
        final Product lentils = product("red-lentils-500g");
        final Product spaghetti = new Product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.49));
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a dinner",
                List.of(new MappedMealSuggestion("Lentil soup", 25, "A complete dinner.", 1,
                        List.of(new MappedProduct(lentils, 1)), BigDecimal.valueOf(1.69))),
                Set.of(0), new Basket(Map.of(spaghetti.slug(), 1))));

        mvc.perform(MockMvcRequestBuilders.post("/demo/checkout/complete").with(csrf()).session(session))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/demo/thank-you").session(session))
                .andExpect(redirectedUrl("/demo?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    private static Product product(final String slug) {
        return new Product(slug, "Red lentils", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.69));
    }
}
