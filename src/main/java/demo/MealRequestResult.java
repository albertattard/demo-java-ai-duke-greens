package demo;

import static demo.Strings.requireNonBlank;

sealed interface MealRequestResult permits MappedMealSuggestions, InvalidRequest, FailedRequest, OutOfScopeRequest { }

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

/// Visitor-facing result for a request outside Duke Greens’ meal-idea scope. It
/// retains the submitted text for editing but deliberately creates no
/// recommendation, selection, or basket state.
record OutOfScopeRequest(String request) implements MealRequestResult {

    OutOfScopeRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}
