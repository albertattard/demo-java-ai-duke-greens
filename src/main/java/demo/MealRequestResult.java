package demo;

import static demo.Strings.requireNonBlank;

sealed interface MealRequestResult permits MealSuggestions, InvalidRequest, FailedRequest { }

record InvalidRequest(String message) implements MealRequestResult {

    InvalidRequest {
        requireNonBlank(message, "The message cannot be blank");
    }
}

record FailedRequest(String request) implements MealRequestResult {

    FailedRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}
