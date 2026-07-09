package demo;

import static java.util.Objects.requireNonNull;

import static demo.Strings.requireNonBlank;

sealed interface MealRequestSessionState permits SuccessfulMealRequest, FailedMealRequest { }

record SuccessfulMealRequest(String request, MappedMealSuggestions suggestions) implements MealRequestSessionState {

    SuccessfulMealRequest {
        requireNonBlank(request, "The request cannot be blank");
        requireNonNull(suggestions, "Suggestions are required");
    }
}

record FailedMealRequest(String request) implements MealRequestSessionState {

    FailedMealRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}
