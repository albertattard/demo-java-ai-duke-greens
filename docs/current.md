# Current change

## Outcome

Let a visitor add the products for individually selected AI-recommended meals to an in-memory basket, so recommendations become an actionable shopping outcome.

## Constraints

- Product catalogue, prices, stock, pack sizes, and basket contents remain application-controlled data; AI may recommend and explain but is not their source of truth.
- Adding a meal’s products to the basket is an explicit visitor action.
- Keep the basket session-scoped and in memory.
- A meal addition is a convenience action; the basket is the visitor’s editable product list.
- Adding a meal records it as selected. It remains selected after basket edits so the application can assess whether the basket fulfils it.
- A visitor can add each displayed recommendation once; do not support duplicate meal additions or removing a selected meal in this slice.
- Do not sell fractional packs. When selected meals require the same product, aggregate their requirements and use the smallest whole-pack quantity that covers the combined amount.
- A visitor may change basket quantities or remove products after adding a meal. The application must show a non-blocking status when the basket no longer contains enough products to fulfil one or more selected meals; the visitor may still proceed.
- Starting a new meal request starts over by clearing the current recommendations, selected meals, basket, and meal-coverage status. The visitor must explicitly confirm this action when it would discard selected meals or basket contents.
- Do not implement checkout, order completion, or meal swapping in this slice.
- Keep the conversation workflow independent of its input and output channel.

## Done when

- A visitor can explicitly add each recommended meal’s products to their basket.
- A previously added recommendation is identified as selected and cannot be added a second time.
- Shared product requirements from selected meals result in the correct whole-pack basket quantity rather than duplicate packs per meal.
- The visitor can increase or decrease a product quantity and remove a product; a line is removed when its quantity reaches zero.
- The basket shows products, quantities, line totals, and total price, recalculating after every change.
- The basket clearly shows whether it contains enough products to fulfil the selected meals without preventing the visitor from continuing when it does not.
- A visitor can start over only after confirming that their selected meals and basket contents will be cleared.
- Application tests cover the primary add-to-basket and shared-product aggregation flows.
- Relevant verification passes.

## Verification

- Add a focused failing test for two selected meals sharing a product, then prove that one whole-pack basket quantity covers their combined requirement.
- Add tests for basket quantity changes, removal, recalculated totals, and the non-blocking meal-coverage status.
- Add a test proving that selected meals remain selected after a basket edit and that a selected recommendation cannot be added twice.
- Add a test proving that starting over requires confirmation before clearing selected meals and basket contents.
- Add a browser integration test from recommendations to an editable consolidated basket.
- Run the project’s documented complete verification suite and `git diff --check`.
