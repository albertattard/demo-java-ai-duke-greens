# TASK-0007: Accept valid responses above the prompt meal limit

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially conversation and recommendation behaviour and acceptance criterion 1.
- [ADR-0003](../adr/ADR-0003-use-openai-structured-output-for-initial-meal-suggestions.md), for structured responses and application validation.

## Outcome

Visitors see every otherwise-valid meal returned by the model, including more than seven meals.

## Visitor value

Direct. An otherwise useful response is displayed rather than discarded solely because the model exceeded its instructed suggestion count.

## Scope

- Retain the system-prompt instruction to return no more than seven meals.
- Remove upper suggestion-count checks from model response records, mapped service results, and session result sets. Do not truncate a valid response.
- Preserve non-null lists, immutable copies, complete meal validation, authoritative catalogue mapping, and all-or-nothing rejection of invalid suggestions.
- Preserve valid zero-suggestion turns and existing conversation, basket, and recovery behaviour.
- Update the specification and affected architecture documentation. The count-related requirements of this task supersede those in [TASK-0004](TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md).

## Out of scope

Changing prompt count interpretation, other validation limits, recovery or correction policy, basket controls, or model configuration.

## Completion criteria

1. A deterministic model response with eight valid meals is accepted and mapped without truncation or a corrective request caused by its count.
2. All eight meals appear on the recommendations page and remain available after refresh without another model call.
3. The system prompt still instructs the model to return no more than seven meals.
4. Existing invalid-response and zero-suggestion coverage remains passing.
5. `./mvnw verify` passes without an OpenAI API key or a live model call.

## Verification

Run the focused count regression before removing the checks, then run the full unit/MVC and browser verification suites. Review the affected specification, ADR, task references, pending-work document, and root README for count-related contradictions.

## Implementation notes

- The eight-meal service regression failed before implementation with the existing zero-to-seven count-validation error.
- Removed count checks at all four record boundaries without truncation. Null lists and incomplete or unmappable meals remain invalid; valid zero-suggestion assistant turns remain supported.
- Model-record tests preserve all eight returned suggestions. Service coverage verifies one generator call and mapping of the full list. Browser coverage displays all eight distinct cards before and after refresh while verifying one generator call. Prompt coverage retains the seven-meal instruction.
- `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 ./mvnw verify` passed: 115 unit/MVC tests and 26 browser tests, packaging, and licence verification. No live model call was made. Chromium required execution outside the sandbox because macOS blocked its process registration inside it; cached Playwright binaries were used.
- Reviewed the affected specification, ADR, task references, pending-work wording, and root README. Local documentation links and `git diff --check` pass.
