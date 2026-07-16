package demo;

import module java.base;

import static demo.Collections.requireSizeBetween;
import static demo.Strings.requireNonBlank;

sealed interface MealRequestResult permits SuccessfulMealSuggestions, InvalidRequest, FailedRequest {}

record SuccessfulMealSuggestions(String assistantMessage,
                                 List<MappedMealSuggestion> suggestions) implements MealRequestResult {

    SuccessfulMealSuggestions {
        requireNonBlank(assistantMessage, "An assistant message is required");
        requireSizeBetween(suggestions, 0, 7, "A response must contain between zero and seven suggestions");

        suggestions = List.copyOf(suggestions);
    }
}

record InvalidRequest(String message) implements MealRequestResult {

    InvalidRequest {
        requireNonBlank(message, "The message cannot be blank");
    }
}

/// Visitor-facing recovery state for a failed request. It retains the original
/// request for explicit retry but deliberately excludes provider diagnostics
/// and partial suggestions, which remain server-side.
record FailedRequest(String request) implements MealRequestResult {

    FailedRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}
