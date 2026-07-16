package demo;

import module java.base;

import static demo.Collections.requireSizeBetween;
import static demo.Strings.requireNonBlank;
import static java.util.Objects.requireNonNull;

sealed interface MealRequestSessionState permits SuccessfulMealRequest, FailedMealRequest, FailedRefinementRequest {}

// TODO: This class seems to be more complicated than needed. Consider revising
//  it.
record SuccessfulMealRequest(
        String request,
        List<MealResultSet> resultSets,
        Set<String> selectedMealKeys,
        Basket basket,
        String pendingRefinement,
        List<ConversationExchange> transcript) implements MealRequestSessionState {

    private static final int MAXIMUM_REFINEMENTS = 10;

    SuccessfulMealRequest {
        requireNonBlank(request, "The request cannot be blank");
        resultSets = List.copyOf(resultSets);
        if (resultSets.isEmpty()) {
            throw new IllegalArgumentException("At least one result set is required");
        }
        selectedMealKeys = Set.copyOf(selectedMealKeys);
        requireNonNull(basket, "A basket is required");
        transcript = List.copyOf(transcript);
        if (transcript.isEmpty()) {
            throw new IllegalArgumentException("At least one conversation exchange is required");
        }
    }

    SuccessfulMealRequest(final String request, final String assistantMessage, final List<MappedMealSuggestion> suggestions) {
        this(request, List.of(new MealResultSet(suggestions, Set.of(), false)), Set.of(), Basket.empty(), null,
                List.of(new ConversationExchange(request, assistantMessage)));
    }

    SuccessfulMealRequest(final String request, final List<MappedMealSuggestion> suggestions) {
        this(request, "Here are some meal ideas.", suggestions);
    }

    SuccessfulMealRequest(final String request, final List<MappedMealSuggestion> suggestions, final Set<Integer> selectedMealIndexes, final Basket basket) {
        this(request, List.of(new MealResultSet(suggestions, Set.of(), false)), selectedMealIndexes.stream().map(index -> key(0, index)).collect(Collectors.toSet()), basket, null,
                List.of(new ConversationExchange(request, "Here are some meal ideas.")));
    }

    SuccessfulMealRequest addMeal(final int mealIndex) {
        return addMeal(0, mealIndex);
    }

    Set<Integer> selectedMealIndexes() {
        return selectedMealKeys.stream()
                .filter(key -> key.startsWith("0:"))
                .map(key -> Integer.parseInt(key.substring(2)))
                .collect(Collectors.toUnmodifiableSet());
    }

    SuccessfulMealRequest addMeal(final int resultSetIndex, final int mealIndex) {
        if (!validMeal(resultSetIndex, mealIndex) || resultSets.get(resultSetIndex).dismissedMealIndexes().contains(mealIndex)) {
            return this;
        }
        final String key = key(resultSetIndex, mealIndex);
        if (selectedMealKeys.contains(key)) {
            return this;
        }
        final Set<String> selected = new HashSet<>(selectedMealKeys);
        selected.add(key);
        return withSelectedMeals(selected);
    }

    SuccessfulMealRequest toggleDismissal(final int mealIndex) {
        final int current = resultSets.size() - 1;
        if (!validMeal(current, mealIndex) || selectedMealKeys.contains(key(current, mealIndex))) {
            return this;
        }
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        updated.set(current, updated.get(current).toggleDismissal(mealIndex));
        return copy(updated, selectedMealKeys, basket, pendingRefinement, transcript);
    }

    SuccessfulMealRequest prepareRefinement(final String refinement) {
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        updated.set(updated.size() - 1, updated.getLast().lockFeedback());
        return copy(updated, selectedMealKeys, basket, refinement, transcript);
    }

    SuccessfulMealRequest appendRefinement(final List<MappedMealSuggestion> suggestions) {
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        updated.add(new MealResultSet(suggestions, Set.of(), false));
        return copy(updated, selectedMealKeys, basket, null, transcript);
    }

    SuccessfulMealRequest appendFollowUp(final String followUp, final String assistantMessage, final List<MappedMealSuggestion> suggestions) {
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        if (!suggestions.isEmpty()) {
            updated.add(new MealResultSet(suggestions, Set.of(), false));
        }
        final List<ConversationExchange> updatedTranscript = new ArrayList<>(transcript);
        updatedTranscript.add(new ConversationExchange(followUp, assistantMessage));
        return copy(updated, selectedMealKeys, basket, null, updatedTranscript);
    }

    SuccessfulMealRequest selectMeals(final Set<String> requestedKeys) {
        final Set<String> selected = requestedKeys.stream()
                .filter(this::selectableMeal)
                .collect(Collectors.toUnmodifiableSet());
        return withSelectedMeals(selected);
    }

    boolean canRefine() {
        return pendingRefinement == null && resultSets.size() - 1 < MAXIMUM_REFINEMENTS;
    }

    Set<String> selectedMealNames() {
        return selectedMealKeys.stream()
                .map(this::mealForKey)
                .map(MappedMealSuggestion::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    Set<String> latestRecommendationNames() {
        return resultSets.getLast().suggestions().stream()
                .map(MappedMealSuggestion::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    Set<String> dismissedMealNames() {
        return resultSets.stream()
                .flatMap(set -> set.dismissedMealIndexes().stream().map(index -> set.suggestions().get(index).name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    boolean selected(final int set, final int meal) {
        return selectedMealKeys.contains(key(set, meal));
    }

    List<MappedMealSuggestion> selectedMeals() {
        return selectedMealKeys.stream().sorted().map(this::mealForKey).toList();
    }

    boolean needsResetConfirmation() {
        return !selectedMealKeys.isEmpty() || !basket.isEmpty();
    }

    private SuccessfulMealRequest copy(final List<MealResultSet> sets, final Set<String> selected, final Basket newBasket, final String pending, final List<ConversationExchange> updatedTranscript) {
        return new SuccessfulMealRequest(request, sets, selected, newBasket, pending, updatedTranscript);
    }

    private SuccessfulMealRequest withSelectedMeals(final Set<String> selected) {
        final List<MappedMealSuggestion> meals = selected.stream().map(this::mealForKey).toList();
        return copy(resultSets, selected, Basket.empty().addRequirements(meals), pendingRefinement, transcript);
    }

    private boolean selectableMeal(final String candidate) {
        try {
            final String[] parts = candidate.split(":");
            if (parts.length != 2) {
                return false;
            }
            final int set = Integer.parseInt(parts[0]);
            final int meal = Integer.parseInt(parts[1]);
            return validMeal(set, meal)
                    && !resultSets.get(set).dismissedMealIndexes().contains(meal);
        } catch (final RuntimeException _) {
            return false;
        }
    }

    private boolean validMeal(final int set, final int index) {
        return set >= 0 && set < resultSets.size() && index >= 0 && index < resultSets.get(set).suggestions().size();
    }

    private MappedMealSuggestion meal(final int set, final int index) {
        return resultSets.get(set).suggestions().get(index);
    }

    private MappedMealSuggestion mealForKey(final String key) {
        final String[] parts = key.split(":");
        return meal(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    static String key(final int set, final int meal) {
        return set + ":" + meal;
    }
}

record ConversationExchange(String visitorMessage, String assistantMessage) {

    ConversationExchange {
        requireNonBlank(visitorMessage, "A visitor message is required");
        requireNonBlank(assistantMessage, "An assistant message is required");
    }
}

record FailedMealRequest(String request) implements MealRequestSessionState {

    FailedMealRequest {
        requireNonBlank(request, "The request cannot be blank");
    }
}

record FailedRefinementRequest(SuccessfulMealRequest request) implements MealRequestSessionState {

    FailedRefinementRequest {
        requireNonNull(request, "The request is required");
    }
}

record MealResultSet(List<MappedMealSuggestion> suggestions, Set<Integer> dismissedMealIndexes,
                     boolean feedbackLocked) {

    MealResultSet {
        requireSizeBetween(suggestions, 0, 7, "A response must contain between zero and seven suggestions");

        dismissedMealIndexes = Set.copyOf(dismissedMealIndexes);
        suggestions = List.copyOf(suggestions);
    }

    MealResultSet toggleDismissal(final int index) {
        if (feedbackLocked || index < 0 || index >= suggestions.size()) {
            return this;
        }

        final Set<Integer> updated = new HashSet<>(dismissedMealIndexes);
        if (!updated.add(index)) {
            updated.remove(index);
        }

        return new MealResultSet(suggestions, updated, false);
    }

    MealResultSet lockFeedback() {
        return new MealResultSet(suggestions, dismissedMealIndexes, true);
    }
}
