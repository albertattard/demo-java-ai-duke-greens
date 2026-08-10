package demo;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static demo.Strings.isBlank;
import static demo.Strings.isLengthBetween;

// TODO: The submit() and refine() methods are very similar. Consider merging
//  these.
@Service
class MealSuggestionService {

    private static final int MINIMUM_MEAL_REQUEST_LENGTH = 1;
    private static final int MAXIMUM_MEAL_REQUEST_LENGTH = 1_000;
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

    MealRequestResult submit(final Request request) {
        if (request == null) {
            return new InvalidRequest("The request must not be null");
        }

        if (request.hasInvalidConversationId()) {
            return new InvalidRequest("The conversation id cannot be empty");
        }

        if (isBlank(request.request)) {
            return new InvalidRequest("Describe at least one meal you want.");
        }

        if (request.hasInvalidRequest()) {
            return new InvalidRequest("Describe your meal request in " + MAXIMUM_MEAL_REQUEST_LENGTH + " characters or fewer.");
        }

        final List<Product> catalogue;
        try {
            catalogue = productCatalogue.allProducts();
        } catch (final RuntimeException e) {
            LOGGER.error("Unable to load the catalogue for a meal suggestion request", e);
            return new FailedRequest(request.request);
        }

        if (catalogue.isEmpty()) {
            LOGGER.error("Cannot generate meal suggestions because the catalogue is empty");
            return new FailedRequest(request.request);
        }

        final ModelMealRequestResponse response;
        try {
            response = generator.suggest(request.toGenerator(catalogue));
        } catch (final RuntimeException e) {
            LOGGER.error("The meal suggestion provider failed", e);
            return new FailedRequest(request.request);
        }

        if (response == null) {
            LOGGER.error("Cannot map meal suggestions because the provider returned no response");
            return new FailedRequest(request.request);
        }

        return mapOrCorrect(request, catalogue, response);
    }

    // Validate the complete model response all-or-nothing. A mapping failure
    // exposes no partial suggestions. Exactly one correction is allowed, and
    // diagnostics remain server-side.
    private MealRequestResult mapOrCorrect(
            final Request request,
            final List<Product> catalogue,
            final ModelMealRequestResponse response) {
        try {
            return map(response, catalogue);
        } catch (final RuntimeException e) {
            final String violatedConstraint = mappingFailureMessage(e);
            LOGGER.warn("The initial meal suggestion response failed catalogue mapping; requesting one correction: {}", violatedConstraint);
            return correct(request, catalogue, violatedConstraint);
        }
    }

    private MealRequestResult correct(
            final Request request,
            final List<Product> catalogue,
            final String violatedConstraint) {
        final ModelMealRequestResponse correctedResponse;
        try {
            correctedResponse = generator.suggest(request.toGenerator(catalogue).correctionFor(violatedConstraint));
        } catch (final RuntimeException e) {
            LOGGER.error("The meal suggestion provider failed during correction", e);
            return new FailedRequest(request.request);
        }

        if (correctedResponse == null) {
            LOGGER.error("Cannot map corrected meal suggestions because the provider returned no response");
            return new FailedRequest(request.request);
        }

        try {
            return map(correctedResponse, catalogue);
        } catch (final RuntimeException e) {
            LOGGER.warn("The corrected meal suggestion response failed catalogue mapping: {}", e.getMessage());
            return new FailedRequest(request.request);
        }
    }

    private String mappingFailureMessage(final RuntimeException failure) {
        return isBlank(failure.getMessage())
                ? "The response contains an invalid meal suggestion."
                : failure.getMessage();
    }

    private SuccessfulMealSuggestions map(final ModelMealRequestResponse response, final List<Product> catalogue) {
        final List<MappedMealSuggestion> suggestions = response.hasSuggestions()
                ? mapper.map(new ModelMealSuggestions(response.suggestions()), catalogue)
                : List.of();
        return new SuccessfulMealSuggestions(response.assistantMessage(), suggestions);
    }

    /// @param recommendations the meal ideas currently shown to the visitor
    /// @param selected        the meal ideas the visitor has chosen to add to their basket
    record Request(
            String conversationId,
            String request,
            Set<String> recommendations,
            Set<String> selected) {

        public Request {
            recommendations = recommendations == null ? Set.of() : Set.copyOf(recommendations);
            selected = selected == null ? Set.of() : Set.copyOf(selected);
        }

        Request(final String conversationId, final String request) {
            this(conversationId, request, Set.of(), Set.of());
        }

        MealSuggestionGenerator.Request toGenerator(final List<Product> catalogue) {
            return new MealSuggestionGenerator.Request(conversationId, request, catalogue, recommendations, selected);
        }

        boolean hasInvalidConversationId() {
            return isBlank(conversationId);
        }

        boolean hasInvalidRequest() {
            return !isLengthBetween(request, MINIMUM_MEAL_REQUEST_LENGTH, MAXIMUM_MEAL_REQUEST_LENGTH);
        }
    }

    void clearConversation(final String conversationId) {
        if (!isBlank(conversationId)) {
            generator.clearConversation(conversationId);
        }
    }
}
