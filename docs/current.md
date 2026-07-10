# Current change

## Status

Complete on 10 July 2026.

## Outcome

Let a visitor with products in their basket select “Proceed to checkout” and review that basket on a dedicated checkout page.

## Constraints

- This slice introduces navigation and basket review only. It does not place, complete, persist, or charge for an order.
- Product details, quantities, prices, and totals on checkout remain application-controlled basket and catalogue data.
- “Proceed to checkout” is shown only when the basket contains at least one product.
- The checkout page shows the basket’s products, quantities, line totals, basket total, and selected-meal coverage status.
- An incomplete basket may be reviewed at checkout, but it must remain clearly identified as not fulfilling all selected meals. This slice must not imply that it can be completed.
- The visitor can return to the recommendations page and continue editing the basket.
- Checkout requires a successful meal-request state with a non-empty basket. A failed request, an empty basket, or missing session state returns the visitor to the initial request page with the same accessible explanation.
- The checkout page provides a “Back to basket” action that returns to `/recommendations` and preserves the active session state.
- Keep visitor state session-scoped and in memory.
- Keep the conversation workflow independent of its input and output channel.

## Done when

- A visitor with a non-empty basket sees a “Proceed to checkout” button on the recommendations page.
- Selecting the button opens a dedicated checkout page showing the authoritative basket contents and total.
- Checkout accurately shows whether the basket fulfils the selected meals.
- A visitor can select “Back to basket” from checkout and edit their basket without losing the active session state.
- A visitor with a failed request, empty basket, or no session state is redirected safely when attempting to open checkout directly.
- Application and browser tests cover the primary results-to-checkout journey and missing-session handling.
- Relevant verification passes.

## Verification

- Add a focused failing MVC test for navigating from a populated basket to checkout.
- Add tests for checkout totals and the insufficient-coverage status.
- Add tests proving that direct checkout requests with a failed request, empty basket, or no session state return to the initial request page.
- Add a browser integration test from meal selection through checkout and back to basket editing.
- Run `./mvnw verify` and `git diff --check`.

## Delivered

- Added a read-only `/checkout` page that uses the session-backed basket and application catalogue as its authoritative data.
- Added guarded checkout navigation, an explicit incomplete-coverage notice, and a return path to the editable basket.
- Added MVC coverage for checkout totals, complete and incomplete meal coverage, and missing, failed, and empty-basket states.
- Added a browser journey from meal selection through checkout and back to basket editing.
- Refactored the request, basket, and checkout routes into focused controllers backed by shared session-state and basket-presentation components.
- Split the welcome and recommendations visitor states into separate templates and controllers. Recommendations no longer show the full product catalogue; basket editing remains on that page. A separate basket page is explicitly deferred to a later slice.

## Verification results

- `./mvnw test -Dtest=MealRequestSessionMvcTest` passed.
- `./mvnw verify` passed with local port binding enabled for the browser integration tests.

## Delivered routes

- Use visitor-facing routes: `GET /recommendations` for successful and failed request states, nested recommendation actions, and `GET /checkout`. Keep `POST /meal-request` as the request-submission command.
