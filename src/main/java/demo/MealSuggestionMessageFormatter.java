package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import demo.MealSuggestionGenerator.Request;

import module java.base;

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
        final StringBuffer userMessage = new StringBuffer();

        if (request.hasSelected()) {
            userMessage.append("The visitor has selected the following meals (soft positive preference)\n");
            userMessage.append(request.selected().stream()
                    .map(selected -> "- " + selected)
                    .collect(Collectors.joining("\n")));
            userMessage.append("\n");
        }

        if (request.hasDismissed()) {
            userMessage.append("The visitor has dismissed the following meals (soft negative preference)\n");
            userMessage.append(request.dismissed().stream()
                    .map(dismissed -> "- " + dismissed)
                    .collect(Collectors.joining("\n")));
            userMessage.append("\n");
        }

        userMessage.append(request.request());

        return userMessage.toString().trim();
    }
}
