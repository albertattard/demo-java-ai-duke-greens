package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("!test & !mock")
class OpenAiMealSuggestionGenerator implements MealSuggestionGenerator {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MealSuggestionMessageFormatter messageFormatter;

    OpenAiMealSuggestionGenerator(
            final ChatClient.Builder chatClientBuilder,
            final MealSuggestionMessageFormatter messageFormatter) {
        // TODO: Consider moving this to a persistant store
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        this.chatClient = chatClientBuilder.build();

        this.messageFormatter = messageFormatter;
    }

    @Override
    public ModelMealRequestResponse suggest(final Request request) {
        return chatClient.prompt()
                .system(messageFormatter.systemMessage(request))
                .messages(chatMemory.get(request.conversationId()))
                .user(messageFormatter.userMessage(request))
                .call()
                .entity(ModelMealRequestResponse.class, spec -> spec.useProviderStructuredOutput());
    }

    @Override
    public void recordSuccessfulResponse(final Request request, final ModelMealSuggestions suggestions) {
        chatMemory.add(request.conversationId(), List.of(
                new UserMessage(request.request()),
                new AssistantMessage(suggestions.suggestions().stream()
                        .map(ModelMealSuggestion::name)
                        .collect(Collectors.joining("\n")))));
    }

    @Override
    public void clearConversation(final String conversationId) {
        chatMemory.clear(conversationId);
    }
}
