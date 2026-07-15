package demo;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDemoAccess.class)
class DemoAccessMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealSuggestionGenerator mealSuggestionGenerator;

    @Test
    void keepsInformationalPagesPublicAndSendsAnonymousVisitorsToDemoAccess() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Try the demo")));
        mvc.perform(get("/about")).andExpect(status().isOk());
        mvc.perform(get("/how-duke-greens-creates-value")).andExpect(status().isOk());
        mvc.perform(get("/capabilities-and-ai-approach")).andExpect(status().isOk());
        mvc.perform(get("/team-and-services")).andExpect(status().isOk());
        mvc.perform(get("/lets-talk")).andExpect(status().isOk());

        mvc.perform(get("/demo"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/demo-access"));
        mvc.perform(get("/demo/recommendations/conversation"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/demo-access"));
        mvc.perform(get("/meal-request")).andExpect(status().isNotFound());
    }

    @Test
    void tellsHtmxToReplaceTheFullPageWithDemoAccess() throws Exception {
        mvc.perform(get("/demo").header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/demo-access"));
    }

    @Test
    void acceptsTheConfiguredAccessCodeAndReturnsToTheSavedDemoPage() throws Exception {
        final MockHttpSession session = (MockHttpSession) mvc.perform(get("/demo"))
                .andReturn().getRequest().getSession(false);

        final MockHttpSession authenticatedSession = (MockHttpSession) mvc.perform(post("/demo-access").session(session).with(csrf()).param("accessCode", TestDemoAccess.accessCode()))
                .andExpect(redirectedUrl("http://localhost/demo?continue"))
                .andReturn().getRequest().getSession(false);
        mvc.perform(get("/demo").session(authenticatedSession)).andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidAccessCodesWithoutAuthenticating() throws Exception {
        mvc.perform(post("/demo-access").with(csrf()).param("accessCode", UUID.randomUUID().toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/demo-access?error"));
    }

    @Test
    void rejectsPinAndLogoutPostsWithoutCsrfTokensAndInvalidatesTheSessionOnLogout() throws Exception {
        mvc.perform(post("/demo-access").param("accessCode", TestDemoAccess.accessCode()))
                .andExpect(status().isForbidden());

        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "conversation");
        mvc.perform(post("/demo/logout").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(post("/demo/logout").session(session).with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
        org.assertj.core.api.Assertions.assertThat(session.isInvalid()).isTrue();
    }
}
