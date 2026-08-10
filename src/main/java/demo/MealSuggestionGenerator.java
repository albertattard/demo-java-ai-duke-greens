package demo;

import module java.base;

import static demo.Collections.requireNonEmpty;
import static demo.Strings.requireNonBlank;

@FunctionalInterface
interface MealSuggestionGenerator {

    ModelMealRequestResponse suggest(Request request);

    default void clearConversation(final String conversationId) {}

    /// @param recommendations the meal ideas currently shown to the visitor
    /// @param selected        the meal ideas the visitor has chosen to add to their basket
    record Request(
            String conversationId,
            String request,
            List<Product> catalogue,
            Set<String> recommendations,
            Set<String> selected,
            String correctionConstraint) {

        public Request {
            requireNonBlank(conversationId, "The conversation ID must not be blank");
            requireNonBlank(request, "The visitor's request must not be blank");
            requireNonEmpty(catalogue, "The catalogue list must not be empty");

            catalogue = List.copyOf(catalogue);
            recommendations = recommendations == null ? Set.of() : Set.copyOf(recommendations);
            selected = selected == null ? Set.of() : Set.copyOf(selected);
            correctionConstraint = correctionConstraint == null ? "" : correctionConstraint;
        }

        Request(final String conversationId, final String request, final List<Product> catalogue, final Set<String> recommendations, final Set<String> selected) {
            this(conversationId, request, catalogue, recommendations, selected, "");
        }

        Request(final String conversationId, final String request, final List<Product> catalogue) {
            this(conversationId, request, catalogue, Set.of(), Set.of(), "");
        }

        boolean hasRecommendations() {
            return !recommendations.isEmpty();
        }

        boolean hasSelected() {
            return !selected.isEmpty();
        }

        boolean isCorrection() {
            return !correctionConstraint.isBlank();
        }

        Request correctionFor(final String violatedConstraint) {
            return new Request(conversationId, request, catalogue, recommendations, selected, violatedConstraint);
        }
    }
}
