# Current change

## Outcome

Visitors see retained meal ideas and a recovery message when refinement fails, rather than a server error.

## Constraints

- Every render of the recommendations template supplies `resetConfirmationRequired` as a boolean.
- Preserve the existing recommendation, reset-confirmation, and failed-refinement behaviour.
- Cover the failed-refinement render path with an MVC test that renders the template.

## Done when

- A request in the failed-refinement state returns a successful recommendations page.
- The page shows the failed-refinement recovery message and does not require a null-to-boolean conversion.

## Verification

- Baseline: `./mvnw verify` passed, including 19 end-to-end tests.
- Added `rendersRetainedMealIdeasWhenRefinementFails` to render the previously failing template path.
- `./mvnw test -Dtest=MealRequestSessionMvcTest` passed with 29 tests.
- `./mvnw verify` passed, including 19 end-to-end tests.
- `git diff --check` passed.
