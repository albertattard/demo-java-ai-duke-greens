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

@WebMvcTest(WelcomePageController.class)
@Import(SecurityConfiguration.class)
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
                .andExpect(redirectedUrl("/meal-request/results"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", suggestions)));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(session))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
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
                .andExpect(redirectedUrl("/meal-request/recovery"))
                .andExpect(request().sessionAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner")));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/recovery").session(session))
                .andExpect(MockMvcResultMatchers.view().name("welcome"))
                .andExpect(model().attribute("failed", true))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(mealSuggestionService).submit("Suggest a vegetarian dinner");
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void returnsToTheInitialPageWhenAResultRouteHasNoSessionState() throws Exception {
        final MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/meal-request/results"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"))
                .andReturn();
        final MvcResult recovery = mvc.perform(MockMvcRequestBuilders.get("/meal-request/recovery"))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
        assertThat(recovery.getRequest().getSession(false)).isNull();
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

        mvc.perform(MockMvcRequestBuilders.post("/meal-request/reset").with(csrf()).session(session))
                .andExpect(redirectedUrl("/"));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/recovery").session(session))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void retriesOnlyWhenTheVisitorExplicitlyPostsTheRetainedFailedRequest() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner"));
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion( "Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        when(mealSuggestionService.submit("Suggest a vegetarian dinner")).thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/meal-request/retry").with(csrf()).session(session))
                .andExpect(redirectedUrl("/meal-request/results"))
                .andExpect(request().sessionAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", suggestions)));

        verify(mealSuggestionService).submit("Suggest a vegetarian dinner");
        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeOneSessionsResultToAnotherSession() throws Exception {
        final MockHttpSession firstSession = new MockHttpSession();
        firstSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner", new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1, List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(new MockHttpSession()))
                .andExpect(redirectedUrl("/?notice=no-active-meal-request"));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(firstSession))
                .andExpect(MockMvcResultMatchers.view().name("welcome"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    private static Product product(final String slug) {
        return new Product(slug, "Red lentils", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.69));
    }
}
