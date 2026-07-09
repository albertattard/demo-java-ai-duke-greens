package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import module java.base;

@Component
final class MealSuggestionSystemMessageFormatter {

    private final String systemMessageTemplate;

    MealSuggestionSystemMessageFormatter(@Value("classpath:prompts/meal-suggestion-system.md") final Resource systemMessageTemplate) {
        try {
            this.systemMessageTemplate = new String(systemMessageTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read the system message", e);
        }
    }

    String format(final List<Product> catalogue) {
        final String formattedProducts = catalogue.stream()
                .map(product -> "- " + product.name() + " (slug: " + product.slug() + ")")
                .collect(Collectors.joining("\n"));

        return systemMessageTemplate.replace("{catalogue}", formattedProducts);
    }
}
