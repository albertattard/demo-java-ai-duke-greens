package demo;

import static demo.Collections.requireNonEmpty;
import static demo.Strings.requireNonBlank;

import module java.base;

@FunctionalInterface
interface MealSuggestionGenerator {

    ModelMealRequestResponse suggest(Request request);

    default void recordSuccessfulResponse(final Request request, final ModelMealSuggestions suggestions) { }

    default void clearConversation(final String conversationId) { }

    record Request(
            String conversationId,
            String request,
            List<Product> catalogue,
            Set<String> dismissed,
            Set<String> selected) {

        public Request {
            requireNonBlank(conversationId, "The conversation ID must not be blank");
            requireNonBlank(request, "The visitor's request must not be blank");
            requireNonEmpty(catalogue, "The catalogue list must not be empty");

            catalogue = List.copyOf(catalogue);
            dismissed = dismissed == null ? Set.of() : Set.copyOf(dismissed);
            selected = selected == null ? Set.of() : Set.copyOf(selected);
        }

        Request(final String conversationId, final String request, final List<Product> catalogue) {
            this(conversationId, request, catalogue, Set.of(), Set.of());
        }

        boolean hasDismissed() {
            return !dismissed.isEmpty();
        }

        boolean hasSelected() {
            return !selected.isEmpty();
        }
    }
}
