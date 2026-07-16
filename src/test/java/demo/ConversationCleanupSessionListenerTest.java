package demo;

import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConversationCleanupSessionListenerTest {

    @Test
    void clearsTheActiveConversationWhenTheSessionIsDestroyed() {
        final MealSuggestionService service = mock(MealSuggestionService.class);
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("mealConversationId", "active-conversation");

        new ConversationCleanupSessionListener(service).sessionDestroyed(new HttpSessionEvent(session));

        verify(service).clearConversation("active-conversation");
    }
}
