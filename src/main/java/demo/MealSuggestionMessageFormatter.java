package demo;

import module java.base;
import demo.MealSuggestionGenerator.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
final class MealSuggestionMessageFormatter {

    private final String systemMessageTemplate;

    MealSuggestionMessageFormatter(@Value("classpath:prompts/meal-suggestion-system.md") final Resource systemMessageTemplate) {
        try {
            this.systemMessageTemplate = new String(systemMessageTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read the system message", e);
        }
    }

    String systemMessage(final Request request) {
        final String formattedProducts = request.catalogue().stream()
                .map(product -> "- " + product.name() + " (slug: " + product.slug() + ")")
                .collect(Collectors.joining("\n"));

        return systemMessageTemplate.replace("{catalogue}", formattedProducts);
    }

    String userMessage(final Request request) {
        final StringBuilder userMessage = new StringBuilder();

        if (request.hasRecommendations()) {
            userMessage.append("The visitor is currently considering the following meal ideas:\n");
            userMessage.append(request.recommendations().stream()
                    .map(dismissed -> "- " + dismissed)
                    .collect(Collectors.joining("\n")));
            userMessage.append("\n\n");
        }

        if (request.hasSelected()) {
            userMessage.append("The visitor has selected the following meals (soft positive preference):\n");
            userMessage.append(request.selected().stream()
                    .map(selected -> "- " + selected)
                    .collect(Collectors.joining("\n")));
            userMessage.append("\n\n");
        }

        userMessage.append("The visitor’s request:\n");
        userMessage.append(request.request());

        return userMessage.toString().trim();
    }
}
