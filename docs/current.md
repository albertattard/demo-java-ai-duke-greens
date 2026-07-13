# Current change

## Outcome

Let a visitor choose which recommended meals are included in a dedicated basket page, where the application derives the required catalogue products and total from that selection, while keeping recommendations focused on choosing and refining meal ideas.

## Constraints

- The product catalogue, prices, selected meals, and derived basket contents remain application-controlled. AI does not read or modify basket state.
- The basket remains scoped to the active meal-request conversation and every basket action must require the conversation UUID to match the browser session. A basket URL is not a portable or shareable basket.
- Before reading or changing basket state, a basket POST validates that its conversation UUID matches the active browser-session conversation UUID. A malformed, stale, or mismatched UUID changes no state and redirects to the active recommendations page with the accessible generic message “This basket is no longer available.” When no active conversation exists, it redirects to the start page with the same message. The response must not reveal whether the supplied UUID exists or belongs to another session.
- Move basket contents, selected-meal controls, the basket total, and reset controls from the recommendations page to a dedicated basket page.
- The basket page lists every non-dismissed recommended meal from every result set with an “Include in basket” checkbox. Checked meals are selected; unchecked meals are not.
- Meals marked “Not for me” are not available for selection on the basket page, because selection would contradict the visitor’s negative feedback.
- The basket’s product lines, whole-pack quantities, and total are derived solely from the checked meals. Shared ingredient requirements are combined before package quantities are rounded.
- This slice has no product-level basket editing or standalone product additions. A visitor changes the basket only by changing meal selection.
- Use one form with an explicit “Update basket” action for the meal checkboxes; do not add JavaScript-driven immediate updates. The action uses POST/Redirect/GET and must not call the model.
- Keep the recommendations page’s refinement workflow and selected-meal preference context intact. Updating the basket changes the selected-meal positive-preference context used by later refinements.
- When a visitor adds a meal from recommendations, replace the meal’s add button with a compact, accessible “Added to basket” state and a clear link to the basket page. Do not use an icon as the only indication or action.
- Removing a meal from the basket page makes its recommendation available to add again. Re-selecting it derives its required products again.
- Replace the recommendations page’s inline basket with a compact, accessible link to the basket page that shows the number of derived product lines and basket total. It remains available while recommendations are displayed.
- Show a checkout action on both the recommendations and basket pages when the derived basket is non-empty. Neither page offers checkout when it is empty.
- The checkout page provides a way back to the active basket page, and the basket page provides a way back to active recommendations.
- An empty basket page explains that meals can be selected there or added from recommendations and must not offer checkout.
- Require explicit visitor confirmation before completing the simulated order; this slice changes navigation to checkout but does not alter order completion.

## Done when

- A visitor can open a dedicated, session-bound basket page from recommendations and see every selectable recommended meal with its current inclusion state.
- Selecting or clearing meal checkboxes and submitting “Update basket” updates the selected meals, derived product lines, quantities, total, and recommendation-page basket summary without calling the model.
- Derived products correctly combine shared ingredient requirements across selected meals before rounding to whole packs.
- A visitor can remove a selected meal on the basket page and add it again from recommendations; re-selecting it correctly restores its derived product requirements.
- Meals marked “Not for me” cannot be selected from the basket page.
- The recommendations page no longer renders editable basket contents, but shows an accessible basket summary link.
- A visitor can proceed to checkout from either recommendations or the basket page when the derived basket is non-empty.
- A visitor can return from checkout to the active basket, then return to the active recommendations without losing the current meal selection.
- Adding a meal collapses its recommendation to an “Added to basket” state with a basket link, without calling the model or ending refinement.
- The selected-meal preference context used for a subsequent refinement reflects basket-page selections.
- A basket POST with a valid active conversation uses POST/Redirect/GET to the active basket or recommendations page. A malformed, stale, or cross-session conversation UUID changes no state and redirects to the active recommendations page, or to the start page when no active conversation exists, with the accessible message “This basket is no longer available.”
- An empty basket has no checkout action and explains that meals can be selected there or added from recommendations.
- Automated coverage verifies the dedicated basket page, checkbox-based meal selection, shared-ingredient derivation, recommendation basket summary and added-meal state, unavailable dismissed meals, selected-meal refinement context, and session-bound POST/Redirect/GET actions.

## Verification

- Start with focused failing coverage for basket-page navigation, selecting and clearing meals through the basket form, shared-ingredient derivation, and rejected stale or cross-session basket actions.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.
