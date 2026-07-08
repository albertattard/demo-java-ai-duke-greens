package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("!test")
class MealSuggestionConfiguration {

    @Bean
    MealSuggestionGenerator openAiMealSuggestionGenerator(
            final ChatClient.Builder chatClientBuilder,
            @Value("classpath:prompts/meal-suggestion-system.md") final Resource systemPrompt) {
        final ChatClient chatClient = chatClientBuilder.build();
        return request -> chatClient.prompt()
                .system(systemPrompt)
                .user(request)
                .call()
                .entity(MealSuggestions.class, spec -> spec.useProviderStructuredOutput());
    }
}
