package demo;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDemoAccess.class)
class ErrorPageMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealSuggestionGenerator mealSuggestionGenerator;

    @Test
    void rendersAStyledSafeRecoveryPageForAMissingPage() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.NOT_FOUND.value()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<title>Page not found | Duke Greens</title>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This page has wandered off.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Start a new meal plan")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Exception"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("a-link-that-does-not-exist"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("timestamp"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("trace"))));
    }

    @Test
    void rendersAStyledSafeRecoveryPageForAnUnexpectedServerError() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Our lettuce has encountered an unexpected situation.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Return to welcome page")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Exception"))));
    }

    @Test
    void rendersAStyledSafeRecoveryPageForAForbiddenRequest() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This page has expired, which can happen after Duke Greens restarts.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Return to welcome page")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Forbidden"))));
    }

    @Test
    void rendersTheFriendlyRecoveryPageWhenCsrfRejectsAMealRequest() throws Exception {
        mvc.perform(post("/demo/meal-request").accept(MediaType.TEXT_HTML)
                        .param("mealRequest", "Suggest a vegetarian dinner"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error"));
    }
}
