# TASK-0006: Preserve meal-request results across safe page refresh

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially the conversation and recommendation behaviour rules and acceptance criterion 12.
- [TASK-0004: Fulfil a one-shot meal request with AI-generated suggestions](TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md), for the existing request, recovery, retry, and reset behaviour.
- [TASK-0005: Map meal ingredients to authoritative catalogue products](TASK-0005-map-meal-ingredients-to-authoritative-catalogue-products.md), for the displayed mapped-suggestion result state.

## Outcome

A visitor can refresh a successful meal-suggestion page or a failed-request recovery page without a browser form-resubmission prompt or another AI request. The generated result or recovery state remains available only in that visitor’s active browser session.

## Visitor value

Direct. Visitors can safely refresh or revisit a page without accidentally repeating their request, seeing inconsistent suggestions, or incurring an unintended AI call.

## Scope

- Change successful and failed meal-request submissions to navigate to distinct GET result and recovery pages after the initial request has been processed.
- Retain the original request and either the accepted mapped suggestions or failure recovery state in a server-managed, in-memory browser session associated with a secure cookie. The state is intentionally unavailable after a server restart.
- Render the retained state from the corresponding GET pages without invoking the model or remapping a result.
- Keep “Try again” as an explicit POST action that resubmits the retained original request exactly once per activation.
- Make “Reset” clear all active browser-session meal-request state and return to the initial request page.
- For a direct result or recovery GET request with no active session state, return to the initial request page with a clear, accessible explanation.
- Add focused tests with a deterministic model boundary that prove the redirect and model-invocation behaviour. Add a browser end-to-end test that reloads both successful and recovery pages without a form-resubmission prompt or another model invocation.

## Out of scope

- Persistence across browser sessions, recovery after server restart, accounts, shared or bookmarkable results, and exposing session identifiers in URLs.
- Changing suggestion generation, validation, catalogue mapping, request-length validation, or card presentation.
- “Show more ideas”, meal selection, basket behaviour, order completion, automatic retries, or follow-up questions.

## Completion criteria

1. A successful meal request invokes the model once and redirects to a GET page that displays the accepted, mapped suggestions retained for the active browser session.
2. Reloading or revisiting that successful GET page does not submit a form, invoke the model, or produce new suggestions.
3. A failed meal request redirects to a GET recovery page that retains its original request. Reloading or revisiting that page does not submit a form or invoke the model.
4. “Try again” is the only recovery action that resubmits the original request. It invokes the model once for each explicit activation.
5. “Reset” clears the request, suggestion, and recovery state. Result and recovery GET requests after reset, or without session state, return to the initial page with an accessible explanation and no stale state.
6. Focused automated tests prove redirect destinations, session-state isolation, and deterministic model invocation counts for successful refresh, failed refresh, explicit retry, reset, and missing-session routes. A tagged browser end-to-end test proves successful and recovery-page refresh behaviour.
7. `./mvnw package` and `./mvnw verify` pass without an OpenAI API key or a live model call.
8. A server restart is not required to preserve an active request. When transient session state is unavailable, the application returns the visitor to the initial request page with an accessible explanation.

## Verification

- Add and run focused failing tests before implementation for a successful POST-to-GET redirect followed by a safe refresh, and for a failed POST-to-GET recovery redirect followed by a safe refresh. Record the failure in the implementation notes or implementation commit body.
- Run the focused unit, MVC/integration, and browser tests after implementation.
- Run `./mvnw package` and `./mvnw verify`.
- Review the root README and affected specifications and task documentation before handover; stop for direction if any are stale or contradicted.

## Implementation notes

- The initial MVC tests for successful and failed meal requests failed as intended because the original controller rendered the POST response directly with no redirect destination.
- Successful and failed requests now use POST/Redirect/GET and retain only the original request plus mapped suggestions or recovery state in the active server-managed browser session. Result and recovery GET handlers do not invoke the model or remap suggestions.
- “Try again” and “Reset” are explicit POST actions. Missing or cleared session state redirects to the initial page with an accessible explanation.
- `./mvnw package` and `./mvnw verify` pass without an OpenAI API key or live model call. The browser verification suite reloads both successful and recovery pages and asserts one model invocation per initial submission.
