package demo;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import module java.base;

@WebMvcTest(WelcomePageController.class)
class MealRequestSessionMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductCatalogue productCatalogue;

    @MockitoBean
    private MealSuggestionService mealSuggestionService;

    @Test
    void redirectsASuccessfulMealRequestToItsSessionBackedResultPage() throws Exception {
        final MappedMealSuggestions suggestions = new MappedMealSuggestions(List.of(new MappedMealSuggestion(
                "Lemon lentil pasta", 25, "A quick dinner.", 1,
                List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))));
        when(mealSuggestionService.submit("Suggest a vegetarian dinner")).thenReturn(suggestions);

        mvc.perform(MockMvcRequestBuilders.post("/meal-request")
                        .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(redirectedUrl("/meal-request/results"));
    }

    @Test
    void redirectsAFailedMealRequestToItsSessionBackedRecoveryPage() throws Exception {
        when(mealSuggestionService.submit("Suggest a vegetarian dinner"))
                .thenReturn(new FailedRequest("Suggest a vegetarian dinner"));

        mvc.perform(MockMvcRequestBuilders.post("/meal-request")
                        .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(redirectedUrl("/meal-request/recovery"));
    }

    @Test
    void returnsToTheInitialPageWhenAResultRouteHasNoSessionState() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results"))
                .andExpect(redirectedUrl("/"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void clearsSessionStateOnReset() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealRequestState", new FailedMealRequest("Suggest a vegetarian dinner"));

        mvc.perform(MockMvcRequestBuilders.post("/meal-request/reset").session(session))
                .andExpect(redirectedUrl("/"));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/recovery").session(session))
                .andExpect(redirectedUrl("/"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    @Test
    void doesNotExposeOneSessionsResultToAnotherSession() throws Exception {
        final MockHttpSession firstSession = new MockHttpSession();
        firstSession.setAttribute("mealRequestState", new SuccessfulMealRequest("Suggest a vegetarian dinner",
                new MappedMealSuggestions(List.of(new MappedMealSuggestion("Lemon lentil pasta", 25, "A quick dinner.", 1,
                        List.of(new MappedProduct(product("red-lentils-500g"), 1)), BigDecimal.valueOf(1.69))))));

        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(new MockHttpSession()))
                .andExpect(redirectedUrl("/"));
        mvc.perform(MockMvcRequestBuilders.get("/meal-request/results").session(firstSession))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("welcome"));

        verifyNoMoreInteractions(mealSuggestionService);
    }

    private static Product product(final String slug) {
        return new Product(slug, "Red lentils", 500, MeasurementUnit.GRAM, BigDecimal.valueOf(1.69));
    }
}
