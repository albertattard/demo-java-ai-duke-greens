# Current change

## Outcome

Let a visitor whose text is not a request for meal ideas receive a clear, safe recovery response that explains Duke Greens’ purpose and helps them submit a relevant request, without presenting invented meal suggestions as an answer to the unrelated request.

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

## Done when

- Submitting an unrelated request displays a clear recovery response on the welcome page.
- The recovery response invites a new meal-planning request and does not claim to have answered the unrelated request.
- An unrelated request cannot reach recommendations or retain or create active visitor meal-request state.
- A mixed request, such as “Suggest dinner and tell me a joke”, receives the same recovery response.
- A subsequent valid meal request proceeds through the existing recommendations flow normally.
- Blank and over-length request validation, meal-suggestion failure recovery, and valid meal-request behaviour remain unchanged.

## Verification

- Start with a focused failing MVC test for an unrelated request’s welcome-page recovery response and absence of retained active state.
- Add focused unit coverage for the explicit out-of-scope result and its handling.
- Add a browser journey that submits an unrelated request, sees the recovery response, replaces it with a meal request, and reaches recommendations.
- Run `./mvnw verify` and `git diff --check`.
