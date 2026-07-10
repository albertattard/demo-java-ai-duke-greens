# Current change

## Outcome

Let a visitor whose text is not a request for meal ideas receive a clear, safe recovery response that explains Duke Greens’ purpose and helps them submit a relevant request, without presenting invented meal suggestions as an answer to the unrelated request. When an unexpected server failure or forbidden request occurs, show a styled Duke Greens error page that offers a safe route back to meal planning without exposing diagnostics.

## Constraints

- This slice handles only requests that are outside meal planning; it does not yet add preference-gathering follow-up questions, conversation history, “Show more ideas”, or voice input and output.
- The application must represent an out-of-scope result explicitly. A prompt instruction alone is not sufficient to determine the visitor-facing outcome.
- A request is in scope only when its sole requested outcome is one or more meal ideas Duke Greens can suggest from its current product catalogue. Requests for information, advice, instructions, actions, or conversation beyond meal ideas are out of scope.
- A request that combines a meal-idea request with any out-of-scope request is out of scope. Duke Greens must not silently answer only its meal-idea portion.
- Representative in-scope requests include “Suggest two quick vegetarian dinners”, “What could I make with lentils?”, and “I need a pasta meal for four”. Representative out-of-scope requests include “What’s the weather?”, “How do I cook risotto?”, “Is tofu in stock?”, “Is this safe for someone with a nut allergy?”, and “Add pasta to my basket”.
- An out-of-scope request must clear any existing active meal-request state and must not create recommendations, selected meals, or basket.
- The response must plainly say “Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two.” It must retain the submitted text for editing or replacement, and must not imply that the unrelated request was answered.
- Keep the text-conversation workflow independent of its input and output channel so voice can use the same business outcome later.
- Keep product catalogue, prices, stock, and basket contents application-controlled; AI remains limited to interpreting meal-planning requests and suggesting catalogue-mappable meals.
- Do not add a keyword-based intent classifier. The boundary must handle ordinary natural-language wording without encoding a fragile list of meal-related terms in the application.
- The 500 and 403 pages must use the existing Duke Greens visual style, include a brief light-hearted message, avoid exception details and technical diagnostics, and provide a clear link to the welcome page. A 403 page must not weaken the POST-only reset flow or CSRF protection.
- The reset confirmation must offer a non-destructive “Keep shopping” action on the left that returns the visitor to the unchanged recommendations and basket; “Clear and start over” remains the destructive action on the right.

## Done when

- Submitting an unrelated request displays a clear recovery response on the welcome page.
- The recovery response invites a new meal-planning request and does not claim to have answered the unrelated request.
- An unrelated request cannot reach recommendations or retain or create active visitor meal-request state.
- A mixed request, such as “Suggest dinner and tell me a joke”, receives the same recovery response.
- A subsequent valid meal request proceeds through the existing recommendations flow normally.
- Blank and over-length request validation, meal-suggestion failure recovery, and valid meal-request behaviour remain unchanged.
- A 500 or 403 response renders the Duke Greens error page with a welcome-page link and no server diagnostics.
- A visitor can decline the reset confirmation with “Keep shopping” and continue with the same selected meals and basket contents.

## Verification

- Start with a focused failing MVC test for an unrelated request’s welcome-page recovery response and absence of retained active state.
- Add focused unit coverage for the explicit out-of-scope result and its handling.
- Add a browser journey that submits an unrelated request, sees the recovery response, replaces it with a meal request, and reaches recommendations.
- Run `./mvnw verify` and `git diff --check`.

## Implementation and verification

- Added a structured generator response with explicit `IN_SCOPE` and `OUT_OF_SCOPE` outcomes; only in-scope results are mapped to catalogue-backed recommendations.
- An out-of-scope outcome clears active meal-request session state and uses flash-backed POST/Redirect/GET to return the visitor to the welcome page with the agreed recovery message and their submitted text retained for replacement; refreshing that page does not repeat the AI request.
- Invalid meal requests use the same flash-backed POST/Redirect/GET pattern, retaining their validation message and submitted text without a browser resubmission warning on refresh.
- Reset confirmation now uses POST/Redirect/GET: the initial request redirects to a confirmation GET page, and only the explicit confirmation POST clears the active request.
- Added MVC and service coverage for explicit out-of-scope and mixed requests, including a failed request that becomes out of scope when retried, plus a browser journey that replaces an unrelated request with a valid meal request and reaches recommendations.
- Added a conventional Spring Boot `error/500` template using the Duke Greens visual style, a light-hearted recovery message, and a welcome-page action without diagnostic details.
- Added MVC coverage that verifies an HTML 500 response uses the custom page and does not expose exception text.
- Added a conventional Spring Boot `error/403` template using the Duke Greens visual style, a light-hearted recovery message that explains an expired page can follow an application restart, and a welcome-page action without diagnostics; CSRF denials explicitly forward to it while retaining CSRF protection, with MVC coverage for the real rejected POST path.
- Added an OpenAI integration test for an unrelated request that asserts an explicit out-of-scope response with no suggestions; it runs only with the `openai-integration` Maven profile and configured credentials.
- Refactored the provider response to a flat, always-present suggestions list: out-of-scope responses use an empty list, while an in-scope response must contain one to seven suggestions. An invalid in-scope response is rejected during provider deserialization and becomes the existing safe generation-failure state.
- Updated the system prompt with explicit in-scope meal-idea examples so the configured model classifies dietary preferences, ingredient requests, preparation time, and servings correctly.
- Aligned the welcome-page “Get meal ideas” action to the right edge of its form without affecting other page actions.
- Added a left-hand “Keep shopping” action to the reset confirmation. It returns to the unchanged recommendations and basket; the destructive “Clear and start over” action is aligned to the far right.
- Added browser coverage that declines the reset confirmation and verifies the selected meal and incomplete basket remain intact before a later confirmed reset.
- Verified with `./mvnw verify -Dit.test=WelcomePageIT#consolidatesSelectedMealsIntoAnEditableBasketWithoutLosingSelection` and `git diff --check`.
- Verified with `./mvnw test`, `./mvnw test -Dtest=MealSuggestionServiceTest,ModelMealSuggestionsTest`, `./mvnw test -Dtest=MealRequestSessionMvcTest`, `./mvnw test -Dtest=ErrorPageMvcTest`, `./mvnw verify`, `./mvnw verify -Popenai-integration -Dit.test=OpenAiMealSuggestionIT`, and `git diff --check`.
