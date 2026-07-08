package demo;

import org.springframework.stereotype.Service;

import static demo.Strings.isBlank;

@Service
class MealSuggestionService {

    private final MealSuggestionGenerator generator;

    MealSuggestionService(final MealSuggestionGenerator generator) {
        this.generator = generator;
    }

    MealRequestResult submit(final String request) {
        if (isBlank(request)) {
            return new InvalidRequest("Describe at least one meal you want.");
        }

        try {
            final MealSuggestions suggestions = generator.suggest(request);
            return suggestions == null
                    ? new FailedRequest(request)
                    : suggestions;
        } catch (final RuntimeException exception) {
            return new FailedRequest(request);
        }
    }
}
