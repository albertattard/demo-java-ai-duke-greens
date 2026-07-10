package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static demo.Strings.isBlank;

import module java.base;

@Service
class MealSuggestionService {

    private static final int MAXIMUM_MEAL_REQUEST_LENGTH = 300;
    private static final Logger LOGGER = LoggerFactory.getLogger(MealSuggestionService.class);

    private final MealSuggestionGenerator generator;
    private final ProductCatalogue productCatalogue;
    private final MealSuggestionMapper mapper;

    MealSuggestionService(
            final MealSuggestionGenerator generator,
            final ProductCatalogue productCatalogue,
            final MealSuggestionMapper mapper) {
        this.generator = generator;
        this.productCatalogue = productCatalogue;
        this.mapper = mapper;
    }

    MealRequestResult submit(final String request) {
        if (isBlank(request)) {
            return new InvalidRequest("Describe at least one meal you want.");
        }

        if (request.length() > MAXIMUM_MEAL_REQUEST_LENGTH) {
            return new InvalidRequest("Describe your meal request in 300 characters or fewer.");
        }

        final List<Product> catalogue;
        try {
            catalogue = productCatalogue.allProducts();
        } catch (final RuntimeException e) {
            LOGGER.error("Unable to load the catalogue for a meal suggestion request", e);
            return new FailedRequest(request);
        }

        if (catalogue.isEmpty()) {
            LOGGER.error("Cannot generate meal suggestions because the catalogue is empty");
            return new FailedRequest(request);
        }

        final ModelMealSuggestions suggestions;
        try {
            suggestions = generator.suggest(request, catalogue);
        } catch (final RuntimeException e) {
            LOGGER.error("The meal suggestion provider failed", e);
            return new FailedRequest(request);
        }

        if (suggestions == null) {
            LOGGER.error("Cannot map meal suggestions because the provider returned no response");
            return new FailedRequest(request);
        }

        // Validate the complete model response all-or-nothing. A mapping
        // failure exposes no partial suggestions; diagnostics remain
        // server-side and the visitor receives the safe recovery state.
        try {
            return mapper.map(suggestions, catalogue);
        } catch (final RuntimeException e) {
            LOGGER.warn("The meal suggestion response could not be mapped to the catalogue. Suggestions: {}", suggestions, e);
            return new FailedRequest(request);
        }
    }
}
