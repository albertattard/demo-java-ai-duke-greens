# Current change

## Outcome

Let a visitor refine meal recommendations conversationally while building a basket, using per-meal “Not for me” feedback without automatically requesting another recommendation.

## Constraints

- Retain the active request’s ordered conversation history in the browser session. It contains the visitor’s initial request and refinements, model responses, and title-only selection and dismissal feedback.
- Each valid refinement sends the stored conversation history to the model, followed by application-controlled current-state context and the visitor’s latest refinement as the current request. A successful response is appended to the history.
- The application-controlled context lists every shown meal title with an instruction not to repeat it, titles added to the basket as soft positive preference data, and titles marked “Not for me” as soft negative preference data. A repeated title is an accepted limitation for this slice; do not reject a response solely because it repeats a title.
- A visitor may toggle “Not for me” on an unselected meal in the current result set. The first toggle collapses the meal to its title; toggling again restores the meal. Neither action calls the model.
- Submitting a refinement finalises “Not for me” feedback for the preceding result set. Earlier result sets remain available for basket selection, but their feedback cannot be changed.
- Keep every result set visible and available during the active request. Defer any grouping or collapsing behaviour until it has been tested in the demo.
- Do not infer ingredient exclusions or dietary restrictions from selected or dismissed meals. Those are a later, separately validated slice.
- Adding a meal to the basket does not end refinement. A visitor may add any non-dismissed meal from an earlier or current result set while preserving existing basket behaviour.
- The initial request, a valid refinement, and an explicit retry of a failed refinement may call the model. “Not for me”, restoring a meal, adding a meal, and editing basket quantities must not call the model.
- Apply the existing trimmed 1–300 character request validation to each refinement. Invalid refinement input does not call the model or change existing state.
- Permit at most ten refinements for an active request. A visitor starts a new meal request to continue after that limit.
- Each refinement asks for the number of meal suggestions in the initial successful result set, capped at seven. The active conversation may contain more than seven meals across its result sets.
- On a failed refinement, preserve the visible conversation history, feedback states, basket, and pending refinement. Show a recoverable failure message with an explicit retry action; retry sends the same stored history and pending refinement without appending that refinement a second time.
- Use POST/Redirect/GET so a refresh neither repeats an AI request nor changes the displayed recommendations.
- The product catalogue remains application-controlled, and every suggested ingredient must continue to map to it before display.
- Show each mapped product as a compact card with its package count, name, package detail, price, and available image; retain an accessible image placeholder where an image is unavailable.
- Place the preparation time beside the meal name, use a decorative clock icon with accessible text, and format durations as compact minutes and hours.
- Place servings beside preparation time, using a decorative person icon and the number alone while preserving accessible serving text.
- Align preparation time and servings to the right of the meal title on wider screens, while keeping them left-aligned when the title row wraps on mobile.

## Done when

- A visitor can toggle “Not for me” for an unselected meal in the current result set without triggering a model request; dismissal collapses the meal and a second toggle restores it.
- A visitor can submit a valid free-text refinement and receive a new result set while retaining every earlier result set and basket action.
- A refinement sends the active request’s stored conversation history, every shown title as a no-repeat instruction, selected titles as soft positive preference data, dismissed titles as soft negative preference data, and the latest refinement as the current request.
- A visitor can add a non-dismissed meal from any result set to the basket and continue refining recommendations.
- Once a refinement is submitted, “Not for me” feedback in the preceding result set is locked while its non-dismissed meals remain available for basket selection.
- A failed refinement preserves the visible conversation, basket, feedback, and pending text; an explicit retry does not duplicate the pending refinement in history.
- Invalid refinement input does not call the model, and an active request permits no more than ten refinements.
- Each refinement requests the initial result-set size, capped at seven, while the conversation can accumulate more meals across result sets.
- Automated coverage verifies dismissal and restoration do not call the model, the history-backed refinement flow, continuing refinement after basket additions, locked earlier feedback, validation and refinement limits, failure retry behaviour, and the per-request suggestion-count contract.
- A visitor can scan a meal’s mapped products as compact catalogue cards without changing the basket action or displayed meal cost.
- A visitor can see a meal’s preparation time alongside its title as “30 min”, “1 hr”, or a combined hour-and-minute duration.
- A visitor can see a meal’s serving count alongside its preparation time without a visible “Serves” prefix.
- A visitor can scan preparation time and servings in a consistent right-aligned position across meal recommendations on wider screens.

## Verification

- Start with focused failing coverage for dismissal and restoration, history-backed refinement, and failure retry.
- Run `./mvnw verify` and `git diff --check`.
