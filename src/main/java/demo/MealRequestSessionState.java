package demo;

import static java.util.Objects.requireNonNull;

import static demo.Strings.requireNonBlank;

import module java.base;

sealed interface MealRequestSessionState permits SuccessfulMealRequest, FailedMealRequest, FailedRefinementRequest {
}

// TODO: This class seems to be more complicated than needed. Consider revising
// it.
record SuccessfulMealRequest(
        String request,
        List<MealResultSet> resultSets,
        Set<String> selectedMealKeys,
        Basket basket,
        String pendingRefinement) implements MealRequestSessionState {

    private static final int MAXIMUM_REFINEMENTS = 10;

    SuccessfulMealRequest {
        requireNonBlank(request, "The request cannot be blank");
        resultSets = List.copyOf(resultSets);
        if (resultSets.isEmpty()) {
            throw new IllegalArgumentException("At least one result set is required");
        }
        selectedMealKeys = Set.copyOf(selectedMealKeys);
        requireNonNull(basket, "A basket is required");
    }

    SuccessfulMealRequest(final String request, final MappedMealSuggestions suggestions) {
        this(request, List.of(new MealResultSet(suggestions, Set.of(), false)), Set.of(), Basket.empty(), null);
    }

    SuccessfulMealRequest(final String request, final MappedMealSuggestions suggestions, final Set<Integer> selectedMealIndexes, final Basket basket) {
        this(request, List.of(new MealResultSet(suggestions, Set.of(), false)), selectedMealIndexes.stream().map(index -> key(0, index)).collect(Collectors.toSet()), basket, null);
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
        return copy(updated, selectedMealKeys, basket, pendingRefinement);
    }

    SuccessfulMealRequest prepareRefinement(final String refinement) {
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        updated.set(updated.size() - 1, updated.getLast().lockFeedback());
        return copy(updated, selectedMealKeys, basket, refinement);
    }

    SuccessfulMealRequest appendRefinement(final MappedMealSuggestions suggestions) {
        final List<MealResultSet> updated = new ArrayList<>(resultSets);
        updated.add(new MealResultSet(suggestions, Set.of(), false));
        return copy(updated, selectedMealKeys, basket, null);
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

    Set<String> dismissedMealNames() {
        return resultSets.stream()
                .flatMap(set -> set.dismissedMealIndexes().stream().map(index -> set.suggestions().suggestions().get(index).name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    boolean selected(final int set, final int meal) {
        return selectedMealKeys.contains(key(set, meal));
    }

    List<MappedMealSuggestion> selectedMeals() {
        return selectedMealKeys.stream().map(this::mealForKey).toList();
    }

    boolean needsResetConfirmation() {
        return !selectedMealKeys.isEmpty() || !basket.isEmpty();
    }

    private SuccessfulMealRequest copy(final List<MealResultSet> sets, final Set<String> selected, final Basket newBasket, final String pending) {
        return new SuccessfulMealRequest(request, sets, selected, newBasket, pending);
    }

    private SuccessfulMealRequest withSelectedMeals(final Set<String> selected) {
        final List<MappedMealSuggestion> meals = selected.stream().map(this::mealForKey).toList();
        return copy(resultSets, selected, Basket.empty().addRequirements(meals), pendingRefinement);
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
        return set >= 0 && set < resultSets.size() && index >= 0 && index < resultSets.get(set).suggestions().suggestions().size();
    }

    private MappedMealSuggestion meal(final int set, final int index) {
        return resultSets.get(set).suggestions().suggestions().get(index);
    }

    private MappedMealSuggestion mealForKey(final String key) {
        final String[] parts = key.split(":");
        return meal(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    static String key(final int set, final int meal) {
        return set + ":" + meal;
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

record MealResultSet(MappedMealSuggestions suggestions, Set<Integer> dismissedMealIndexes, boolean feedbackLocked) {

    MealResultSet {
        requireNonNull(suggestions, "Suggestions are required");
        dismissedMealIndexes = Set.copyOf(dismissedMealIndexes);
    }

    MealResultSet toggleDismissal(final int index) {
        if (feedbackLocked || index < 0 || index >= suggestions.suggestions().size()) {
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
