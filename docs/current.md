# Current change

## Outcome

Let a visitor whose text is not a request for meal ideas receive a clear, safe recovery response that explains Duke Greens’ purpose and helps them submit a relevant request, without presenting invented meal suggestions as an answer to the unrelated request.

## Constraints

- This slice handles only requests that are outside meal planning; it does not yet add preference-gathering follow-up questions, conversation history, “Show more ideas”, or voice input and output.
- The application must represent an out-of-scope result explicitly. A prompt instruction alone is not sufficient to determine the visitor-facing outcome.
- An out-of-scope request must not create an active meal-request session, recommendations, selected meals, or basket.
- The response must plainly say that Duke Greens helps with meal ideas and invite the visitor to describe the meals they want; it must not imply that the unrelated request was answered.
- The visitor must remain able to edit or replace their request and submit again from the welcome page.
- Keep the text-conversation workflow independent of its input and output channel so voice can use the same business outcome later.
- Keep product catalogue, prices, stock, and basket contents application-controlled; AI remains limited to interpreting meal-planning requests and suggesting catalogue-mappable meals.
- Do not add a keyword-based intent classifier. The boundary must handle ordinary natural-language wording without encoding a fragile list of meal-related terms in the application.

## Done when

- Submitting an unrelated request displays a clear recovery response on the welcome page.
- The recovery response invites a new meal-planning request and does not claim to have answered the unrelated request.
- An unrelated request cannot reach recommendations or create active visitor meal-request state.
- A subsequent valid meal request proceeds through the existing recommendations flow normally.
- Blank and over-length request validation, meal-suggestion failure recovery, and valid meal-request behaviour remain unchanged.

## Verification

- Start with a focused failing MVC test for an unrelated request’s welcome-page recovery response and absence of retained active state.
- Add focused unit coverage for the explicit out-of-scope result and its handling.
- Add a browser journey that submits an unrelated request, sees the recovery response, replaces it with a meal request, and reaches recommendations.
- Run `./mvnw verify` and `git diff --check`.
