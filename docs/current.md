# Current change

## Outcome

Visitors with an active meal-discovery conversation are returned to its recommendations page instead of seeing the welcome page. They use the existing Start over action to clear the conversation before returning to the welcome page.

## Constraints

- An active conversation is identified by the session-held conversation ID.
- Preserve the existing reset confirmation when selected meals require it.
- A direct meal-request POST during an active conversation clears its session state and AI memory before handling the new request.
- Product data, prices, stock, basket contents, and order state remain application-controlled.

## Done when

- `GET /demo` redirects an active session to its recommendations URL.
- `POST /demo/meal-request` clears an active session and starts a new request.
- Completing Start over clears the conversation and shows the welcome page.

## Verification

- Baseline verification: `./mvnw package` passed with 100 tests.
- Focused MVC coverage for active-session navigation passed: `./mvnw test -Dtest=MealRequestSessionMvcTest` with 28 tests.
- `./mvnw package` passed with 100 tests; `./mvnw verify` passed with 100 unit/MVC tests and 20 browser integration tests.
