# Current change

## Status

Agreed on 10 July 2026; not started.

## Outcome

Let a visitor with products in their basket select “Proceed to checkout” and review that basket on a dedicated checkout page.

## Constraints

- This slice introduces navigation and basket review only. It does not place, complete, persist, or charge for an order.
- Product details, quantities, prices, and totals on checkout remain application-controlled basket and catalogue data.
- “Proceed to checkout” is shown only when the basket contains at least one product.
- The checkout page shows the basket’s products, quantities, line totals, basket total, and selected-meal coverage status.
- An incomplete basket may be reviewed at checkout, but it must remain clearly identified as not fulfilling all selected meals. This slice must not imply that it can be completed.
- The visitor can return to the meal-results page and continue editing the basket.
- Checkout requires a successful meal-request state with a non-empty basket. A failed request, an empty basket, or missing session state returns the visitor to the initial request page with the same accessible explanation.
- The checkout page provides a “Back to basket” action that returns to `/meal-request/results` and preserves the active session state.
- Keep visitor state session-scoped and in memory.
- Keep the conversation workflow independent of its input and output channel.

## Done when

- A visitor with a non-empty basket sees a “Proceed to checkout” button on the results page.
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
