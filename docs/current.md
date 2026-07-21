# Current change

## Outcome

A visitor can describe a detailed meal request of up to 1,000 characters and see a live character counter, so they can use the available space deliberately before asking for meal ideas.

## Constraints

- Enforce a 1,000-character limit consistently in the browser and server-side validation. The counter must report characters, not words, because characters are what the application limits.
- The counter updates as the visitor types or dictation changes the textarea, starts at zero, and remains understandable to assistive technology without interrupting ordinary typing.
- Keep the current request, dictation, and submission workflow intact. The visitor still explicitly submits before any meal ideas are requested.
- Keep the existing OpenAI integration-test changes as focused coverage for the reported long meal-planning request. They remain opt-in and normal verification must not call OpenAI.

## Done when

- The welcome-page meal-request textarea accepts up to 1,000 characters and rejects a longer submitted request with a clear validation message.
- A visible live counter reports the current character count and maximum, including after dictated text is inserted.
- Browser coverage proves the initial counter, typed update, dictated update, and 1,000-character boundary; server-side coverage proves the rejection boundary.
- `OpenAiMealSuggestionIT` retains the detailed reported request and robust catalogue-valid assertions for both turns.

## Implementation and verification

- Increased the welcome meal-request limit to 1,000 characters in the browser and the application service, including exact-boundary and over-limit validation coverage.
- Added an accessible, visible character counter associated with the textarea. It updates after typing and after dictation inserts or restores text, without becoming a live-announcement region during ordinary typing. It is compact and right-aligned with the textarea.
- Preserved the existing opt-in OpenAI clarification regression coverage as part of this slice.
- Verified with `./mvnw verify`.
