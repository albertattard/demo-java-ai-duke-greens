# Current change

## Outcome

Let a visitor with a complete basket explicitly complete a simulated order, see a dedicated thank-you page, and return to the welcome page to begin again.

## Constraints

- Completing an order is simulated: it must not charge the visitor, persist an order, or change catalogue or stock data.
- The checkout action must plainly communicate that it completes a simulated order; selecting it is the visitor’s explicit confirmation.
- Only a successful meal request with a non-empty basket that fulfils the selected meals may be completed.
- On simulated completion, clear the active meal-request state, including selected meals and basket contents; retain unrelated session data.
- The thank-you page is only available immediately after a valid simulated completion, does not disclose the prior basket, and provides a return action to the welcome page.
- On checkout, the return and completion actions share one row, with completion aligned to the right; narrow viewports may stack them to remain usable.
- “Back to basket” is visually secondary to the simulated-order completion action.
- On recommendations, “Start over” and “Proceed to checkout” share one row when the basket is non-empty, with checkout aligned to the right; narrow viewports may stack them to remain usable.
- “Start over” is visually secondary to the checkout action while retaining its confirmation step.
- Enabling work: a `mock` Spring profile supports offline manual flow testing without OpenAI credentials or network calls. It returns one or more random valid recommendations from a fixed local set, regardless of the visitor’s request.
- Keep the conversation workflow independent of its input and output channel.

## Done when

- A complete checkout displays a “Complete simulated order” action.
- Selecting that action shows a dedicated thank-you page confirming the simulated order.
- Incomplete baskets cannot display or invoke simulated completion.
- The prior active request, selected meals, and basket are absent after completion.
- The thank-you page safely rejects direct access without a preceding valid completion.
- A visitor can return from the thank-you page to the welcome page and start a fresh request.
- On a complete checkout, “Back to basket” is on the left and “Complete simulated order” is on the right of the same action row.
- “Back to basket” uses a neutral outlined style, distinct from the green primary completion action.
- With a non-empty basket, “Start over” is on the left and “Proceed to checkout” is on the right of the same recommendations-page action row.
- “Start over” uses a neutral outlined style, distinct from the green primary checkout action.
- Starting with the `mock` profile succeeds without OpenAI configuration and supplies catalogue-valid meal suggestions for arbitrary input.

## Verification

- Add a focused failing MVC test for completing a valid checkout and reaching the thank-you page.
- Add MVC tests for incomplete checkout and invalid direct thank-you access.
- Add a browser journey from meal selection through complete checkout, thank-you, and a fresh welcome page.
- Add a browser assertion for the checkout action row layout.
- Add a browser assertion for the recommendations action row layout.
- Add a Spring integration test for the offline mock generator and document how to start it.
- Run `./mvnw verify` and `git diff --check`.

## Delivered

- Added a guarded “Complete simulated order” action for baskets that fulfil the selected meals.
- Added a dedicated, one-time thank-you page that confirms no payment was taken and returns the visitor to the welcome page.
- Simulated completion clears the active meal request, selected meals, and basket while retaining unrelated session data.
- Added MVC coverage for valid completion, incomplete checkout, direct thank-you access, and consumed thank-you access.
- Added a browser journey covering meal selection, completion, thank-you, and a fresh welcome page.
- Placed checkout’s return and completion actions on one responsive row, with completion aligned right on wider viewports.
- Styled “Back to basket” as a neutral outlined secondary action.
- Placed recommendations’ “Start over” and “Proceed to checkout” actions on one responsive row, with checkout aligned right on wider viewports.
- Styled “Start over” as a neutral outlined secondary action.
- Added the offline `mock` profile and `MockMealSuggestionGenerator`, which selects one to three catalogue-valid recommendations at random from a fixed local set without reading the request or calling OpenAI.
- Documented the offline manual-test command in the repository README.

## Verification results

- Baseline: `./mvnw verify` passed.
- After implementation: `./mvnw test -Dtest=MealRequestSessionMvcTest` passed.
- After implementation: `./mvnw verify` and `git diff --check` passed.
- After action-row refinement: `./mvnw verify -Dit.test=WelcomePageIT` and `git diff --check` passed.
- After recommendations action-row refinement: `./mvnw verify -Dit.test=WelcomePageIT` and `git diff --check` passed.
- After secondary-action styling: `./mvnw verify -Dit.test=WelcomePageIT` and `git diff --check` passed.
- After checkout secondary-action styling: `./mvnw verify -Dit.test=WelcomePageIT` and `git diff --check` passed.
- Mock-profile coverage: `./mvnw test -Dtest=MockMealSuggestionGeneratorTest` passed.
- After mock-profile implementation: `./mvnw verify` and `git diff --check` passed.
