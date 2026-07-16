package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test & !mock")
class OpenAiMealSuggestionGenerator implements MealSuggestionGenerator {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MealSuggestionMessageFormatter messageFormatter;

    OpenAiMealSuggestionGenerator(
            final ChatClient.Builder chatClientBuilder,
            final MealSuggestionMessageFormatter messageFormatter) {
        // TODO: Consider moving this to a persistent store
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        this.messageFormatter = messageFormatter;
    }

    @Override
    public ModelMealRequestResponse suggest(final Request request) {
        return chatClient.prompt()
                .system(messageFormatter.systemMessage(request))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .user(messageFormatter.userMessage(request))
                .call()
                .entity(ModelMealRequestResponse.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
    }

    @Override
    public void clearConversation(final String conversationId) {
        chatMemory.clear(conversationId);
    }
}
