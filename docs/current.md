# Current change

## Outcome

Visitors can submit a meal request and log out from one aligned action row inside the Duke Greens meal-request card.

## Constraints

- Keep meal-request submission and logout as separate POST forms so their existing CSRF protection and actions remain unchanged.
- Place both actions in the meal-request card and align them horizontally at wider viewports, with “Log out” left and “Get meal ideas” right.
- Retain the agreed card and typography treatments.

## Done when

- The meal-request card contains both “Log out” and “Get meal ideas”.
- The two actions share one horizontal action row at wider viewports, with “Log out” left and “Get meal ideas” right.
- The “Log out” and “Get meal ideas” controls still submit their respective existing POST actions.

## Verification

- Baseline: `./mvnw verify` passed, including 23 tests.
- The new browser assertion failed before implementation because the meal-request action row did not exist.
- `./mvnw verify` passed after implementation, including 23 tests.
- `./mvnw verify` passed after aligning “Log out” left and “Get meal ideas” right, including 23 tests.
