package demo;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
class ConversationCleanupSessionListener implements HttpSessionListener {

    private final MealSuggestionService mealSuggestionService;

    ConversationCleanupSessionListener(final MealSuggestionService mealSuggestionService) {
        this.mealSuggestionService = mealSuggestionService;
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final Object conversationId = event.getSession().getAttribute("mealConversationId");
        if (conversationId instanceof final String activeConversationId) {
            mealSuggestionService.clearConversation(activeConversationId);
        }
    }
}
