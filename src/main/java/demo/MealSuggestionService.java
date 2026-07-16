package demo;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static demo.Strings.isBlank;

// TODO: The submit() and refine() methods are very similar. Consider merging
//  these.
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

    MealRequestResult submit(final String conversationId, final String request) {
        if (isBlank(conversationId)) {
            return new FailedRequest("The conversation id must not be null");
        }

        final Optional<String> validationError = validationError(request);
        if (validationError.isPresent()) {
            return new InvalidRequest(validationError.get());
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

        final ModelMealRequestResponse response;
        try {
            response = generator.suggest(new MealSuggestionGenerator.Request(conversationId, request, catalogue));
        } catch (final RuntimeException e) {
            LOGGER.error("The meal suggestion provider failed", e);
            return new FailedRequest(request);
        }

        if (response == null) {
            LOGGER.error("Cannot map meal suggestions because the provider returned no response");
            return new FailedRequest(request);
        }

        // The model determined that the request is out of scope and cannot be
        // answered without breaking the policy defined by the system message.
        if (response.isOutOfScope()) {
            return new OutOfScopeRequest(request);
        }

        // Validate the complete model response all-or-nothing. A mapping
        // failure exposes no partial suggestions; diagnostics remain
        // server-side and the visitor receives the safe recovery state.
        try {
            final List<MappedMealSuggestion> suggestions = mapper.map(response.inScopeSuggestions(), catalogue);
            return new SuccessfulMealSuggestions(response.assistantMessage(), suggestions);
        } catch (final RuntimeException e) {
            LOGGER.warn("The meal suggestion response could not be mapped to the catalogue. Suggestions: {}", response.suggestions(), e);
            return new FailedRequest(request);
        }
    }

    MealRequestResult followUp(
            final String conversationId,
            final String followUp) {
        if (isBlank(conversationId)) {
            return new FailedRequest("The conversation id must not be null");
        }

        if (isBlank(followUp)) {
            return new InvalidRequest("Describe what you’d like to change about the meal ideas.");
        }

        if (followUp.length() > MAXIMUM_MEAL_REQUEST_LENGTH) {
            return new InvalidRequest("Describe your meal request in 300 characters or fewer.");
        }

        final List<Product> catalogue;
        try {
            catalogue = productCatalogue.allProducts();
        } catch (final RuntimeException e) {
            LOGGER.error("Unable to load the catalogue for a refinement", e);
            return new FailedRequest(followUp);
        }

        if (catalogue.isEmpty()) {
            LOGGER.error("Cannot generate refined meal suggestions because the catalogue is empty");
            return new FailedRequest(followUp);
        }

        final ModelMealRequestResponse response;
        try {
            response = generator.suggest(new MealSuggestionGenerator.Request(conversationId, followUp, catalogue));
        } catch (final RuntimeException e) {
            LOGGER.error("The meal refinement provider failed", e);
            return new FailedRequest(followUp);
        }

        if (response == null || response.isOutOfScope()) {
            LOGGER.warn("The meal refinement provider returned an unusable response");
            return new FailedRequest(followUp);
        }

        try {
            final List<MappedMealSuggestion> suggestions = mapper.map(response.inScopeSuggestions(), catalogue);
            return new SuccessfulMealSuggestions(response.assistantMessage(), suggestions);
        } catch (final RuntimeException e) {
            LOGGER.warn("The meal refinement response could not be used. Suggestions: {}", response.suggestions(), e);
            return new FailedRequest(followUp);
        }
    }

    static Optional<String> validationError(final String request) {
        if (isBlank(request)) {
            return Optional.of("Describe at least one meal you want.");
        }

        if (request.length() > MAXIMUM_MEAL_REQUEST_LENGTH) {
            return Optional.of("Describe your meal request in 300 characters or fewer.");
        }

        return Optional.empty();
    }

    void clearConversation(final String conversationId) {
        if (!isBlank(conversationId)) {
            generator.clearConversation(conversationId);
        }
    }
}
