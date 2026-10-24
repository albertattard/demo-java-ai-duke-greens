# Current change

## Outcome

Visitors see every otherwise-valid meal returned by the model, including responses with more than seven meals.

## Constraints

- Retain the system-prompt instruction to return no more than seven suggestions.
- Remove application-side suggestion-count rejection without truncating responses or weakening other response and catalogue validation.
- Preserve valid zero-suggestion turns and existing conversation, basket, and recovery behaviour.
- Update the MVP specification and affected architecture documentation, and record delivery in TASK-0007.
- Leave the earlier `.gitignore` change separate and untouched.

## Done when

- Deterministic tests prove that an eight-meal model response is mapped successfully and all eight meals are displayed and retained across refresh.
- A focused test proves that the system prompt still instructs the model to return no more than seven meals.
- Existing validation and conversation tests and the full verification suite pass without a live model call.

## Baseline

`./mvnw test` passed with 115 tests before implementation.

## Implementation and verification

Removed the upper suggestion-count checks from model response records, mapped service results, and session result sets. Non-null lists, immutable copies, complete meal validation, catalogue mapping, and valid zero-suggestion turns remain supported. The system prompt is unchanged and its seven-meal instruction is covered by a focused assertion. The MVP specification, ADR-0003, deferred-work wording, and task index now distinguish prompt guidance from application validation; [TASK-0007](tasks/TASK-0007-accept-valid-responses-above-the-prompt-meal-limit.md) records delivery and supersedes TASK-0004’s original count rules.

The new eight-meal service regression failed before implementation on the existing count check. Updated model-record coverage proves that every supplied suggestion is retained; service coverage proves acceptance without a corrective call caused by count; browser coverage proves that all eight distinct meal cards survive refresh without another model request. `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 ./mvnw verify` passed with 115 unit/MVC tests and 26 browser tests, including packaging and licence verification, without a live model call. Browser verification required execution outside the sandbox because macOS blocked Chromium process registration inside it. Reviewed the affected documentation and root README, checked local documentation links, and ran `git diff --check` successfully.
