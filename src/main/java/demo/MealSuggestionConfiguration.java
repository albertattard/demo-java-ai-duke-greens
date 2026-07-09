package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
class MealSuggestionConfiguration {

    @Bean
    MealSuggestionGenerator openAiMealSuggestionGenerator(
            final ChatClient.Builder chatClientBuilder,
            final MealSuggestionSystemMessageFormatter systemMessageFormatter) {
        final ChatClient chatClient = chatClientBuilder.build();

        return (request, catalogue) -> chatClient.prompt()
                .system(systemMessageFormatter.format(catalogue))
                .user(request)
                .call()
                .entity(ModelMealSuggestions.class, spec -> spec.useProviderStructuredOutput());
    }
}
