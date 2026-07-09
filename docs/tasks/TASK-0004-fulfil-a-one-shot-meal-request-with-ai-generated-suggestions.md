# TASK-0004: Fulfil a one-shot meal request with AI-generated suggestions

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially the included text conversation and visitor-flow steps 1–4.
- [Duke Greens product vision](../product/vision.md), especially the target experience and product rules.
- [TASK-0002: Present the welcome and request input](TASK-0002-present-welcome-and-request-input.md).
- [ADR-0003: Use OpenAI structured output for initial meal suggestions](../adr/ADR-0003-use-openai-structured-output-for-initial-meal-suggestions.md).

## Outcome

A visitor can make one non-empty free-form meal request and receive AI-generated meal suggestions that fulfil the requested number of meals up to a maximum of seven. Each suggestion shows its name, preparation time, a short explanation, servings, and structured ingredients with quantities and units.

For example, “Suggest three quick vegetarian dinners for one person” shows three suggestions; “Suggest a meal” shows one. This is a one-shot interaction after a successful response. A failure offers only recovery actions: retrying the same request or resetting.

## Visitor value

Direct. A visitor receives practical meal ideas tailored to their written request, demonstrating the AI-assisted part of Duke Greens without making unverified claims about products, prices, or orders.

## Scope

- Add a browser end-to-end test, tagged `@Tag("e2e")`, that submits a representative request and asserts that the appropriate number of suggestions, capped at seven, and their structured information are visible.
- Add a model-facing boundary that accepts the visitor’s request and receives structured meal suggestions.
- Send the visitor’s free-form request to the model without application-side quantity parsing. Instruct the model to interpret the requested number of meals, default to one when no count is stated, and return no more than seven suggestions.
- Require every displayed suggestion to contain a name, preparation time, short explanation, servings, and one or more ingredients with a quantity and unit.
- Display the suggestions accessibly and make clear that they are meal ideas, not product, price, stock, basket, or order commitments.
- Reject blank or whitespace-only submissions with a visible, accessible validation message.
- Validate the model response before presentation. For a timeout, provider failure, malformed response, no suggestions, or more than seven suggestions, show an accessible friendly error without partial suggestions or provider error details.
- Provide `Try again` as a distinct visitor action that resubmits the same request, and `Reset` to clear the failed request and return to the initial input state. Do not retry automatically.
- Use a deterministic fake at the model-facing boundary for automated tests. Normal Maven verification must not require an API key or a live model call.
- Provide an opt-in Maven profile that makes one live OpenAI request against the configured model and verifies that it produces a complete, structured meal suggestion. The profile must remain inactive during normal Maven verification.

## Out of scope

- Mapping ingredients to catalogue products, package quantities, prices, availability, basket creation, product selection, order confirmation, or any external state change.
- Follow-up questions, replacement suggestions, successful-flow reset behaviour, and a multi-turn conversation.
- Any guarantee that a suggestion is suitable for a dietary, medical, allergy, nutritional, or preparation-time constraint.
- Cross-browser-session persistence, accounts, analytics, and client-side conversational state management.

## Completion criteria

1. A visitor can submit one non-empty text request from the landing page.
2. The visitor sees the number of meal suggestions requested when it is one to seven, one when no count is stated, and seven when the request exceeds seven.
3. Each displayed suggestion has a name, preparation time, short explanation, servings, and structured ingredient quantities and units.
4. No displayed suggestion claims a catalogue product match, package price, stock availability, basket, or order.
5. The page provides no additional-request, selection, basket-changing, or order-completion control after the one-shot response.
6. A request for more than seven meals results in seven suggestions; the application does not parse the free-form text to enforce that limit.
7. A blank or whitespace-only request results in an accessible validation message.
8. A timeout, provider failure, malformed response, no suggestions, or more than seven suggestions results in an accessible friendly error with no partial suggestions or provider error details.
9. `Try again` is a visitor action that resubmits the same request; `Reset` returns the visitor to the initial input state. Neither action occurs automatically.
10. Automated tests cover requests for one, seven, and more than seven meals, plus malformed, empty, and over-seven model responses, using a deterministic fake model boundary; a browser end-to-end test proves the main request-to-suggestions flow.
11. `./mvnw package` and `./mvnw verify` pass without an OpenAI API key or live model call.
12. `./mvnw verify --activate-profiles openai-integration` runs a tagged live-provider integration test when an OpenAI API key is available from the local external configuration.

## Verification

- Before implementation, add and run the focused end-to-end test for a submitted request and its suggestions; record its failure in this task’s implementation notes or the implementation commit body.
- Run the focused unit and browser tests after implementation.
- Run `./mvnw package` and `./mvnw verify`.
- With a configured local OpenAI API key, run `./mvnw verify --activate-profiles openai-integration` and confirm the live structured-output request succeeds.
- Review the root README and affected documentation for any new session or local-run behaviour.

## Implementation notes

- The initial end-to-end test was added before implementation and failed because the landing page did not yet provide a submission action.
- Spring AI 2.0.0 is used because it supports the repository’s Spring Boot 4.1 baseline; the previously available 1.1.2 release does not.
- The OpenAI generator is active outside the `test` profile and receives its key from external Spring configuration supplied at launch. Automated tests activate the `test` profile and inject a deterministic Mockito `MealSuggestionGenerator` boundary, so verification neither reads local configuration nor invokes a live model.
- The `openai-integration` Maven profile sets the external configuration import and runs only the tagged live-provider test. It is intentionally opt-in because it requires credentials, network access, and incurs API cost.
- `./mvnw verify --activate-profiles openai-integration` completed successfully with the locally configured OpenAI API key, including one live structured-output request.
- The committed application configuration contains no user-home import. A local run and the opt-in Maven profile each supply the same import explicitly before Spring Boot starts.
