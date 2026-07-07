# TASK-0002: Present the welcome and request input

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), visitor-flow step 1 and acceptance criterion 1.
- [Duke Greens product vision](../product/vision.md), target experience and MVP scope.
- [ADR-0001: Adopt the initial technical stack](../adr/ADR-0001-adopt-the-initial-technical-stack.md).

## Outcome

When a visitor opens Duke Greens, they see a welcome message and a text input through which they can describe the meals they want.

## Visitor value

Direct. It gives a stand visitor a clear entry point into the conversational shopping journey and makes the demo’s purpose understandable to nearby observers.

## Scope

- Add a Playwright browser end-to-end integration test, named with the Failsafe `*IT` suffix and tagged `@Tag("e2e")`, that opens the application and asserts the welcome message and visible meal-request text input.
- Add the Playwright dependencies, browser prerequisites, and Maven configuration needed to run end-to-end tests without a profile.
- Configure Surefire to exclude JUnit tests tagged `e2e` and Failsafe to run those tagged tests during the integration-test and verify lifecycle phases, so `./mvnw package` runs unit tests only and `./mvnw verify` runs both unit and end-to-end integration tests.
- Run the test and record that it fails before visitor-facing implementation is added.
- Implement only the server-rendered initial page and styling necessary for that test to pass.
- Keep the page text-based and suitable for the visible conversation area specified by the MVP.

## Browser setup decision

End-to-end tests use the Playwright-managed Chromium binary rather than a
system-installed browser. This makes the supported browser version part of the
project's test tooling and avoids machine-specific executable paths. The root
README must document the one-time Playwright Chromium installation command;
Maven test lifecycles must not download browsers implicitly.

## Out of scope

- Submitting or interpreting the request, recommendations, catalogue access, AI integration, meal cards, basket behaviour, reset, and order completion.
- Client-side state management, voice interaction, user accounts, and any excluded MVP capability.

## Completion criteria

1. A Playwright end-to-end integration test named with the Failsafe `*IT` suffix and tagged `@Tag("e2e")` covers opening the application’s initial page.
2. Playwright browser prerequisites are documented in the [root README](../../README.md).
3. The test asserts a visitor-visible welcome message that identifies Duke Greens or its meal-planning purpose.
4. The test asserts an accessible, visible text input for a meal-planning request.
5. The test demonstrably failed before the initial page was implemented and passes after the minimal implementation.
6. `./mvnw package` passes with unit tests only, and `./mvnw verify` passes with both unit and end-to-end integration tests, without an OpenAI API key or live model call.

## Verification

- Run the focused Playwright test through Maven before implementation and retain the failure result in the task’s implementation notes or commit body.
- Run `./mvnw verify` after implementation.
- Run `./mvnw package` and `./mvnw verify` as the complete documented verification suite.

## Implementation notes

- Before adding the initial page, `./mvnw verify` failed on 2026-07-07 in
  `WelcomePageIT.presentsWelcomeAndMealRequestInput`: the expected Duke Greens
  heading was not visible at `/`.
- After implementation, `./mvnw package` passed with the unit-test suite only
  and `./mvnw verify` passed with both the unit-test suite and the tagged
  `WelcomePageIT` browser integration test.
- The browser test was then refactored to use a local Playwright harness and
  fluent page methods; `./mvnw package` and `./mvnw verify` passed again.
- The page assertions use Playwright's retrying locator assertions with exact
  accessible-name matching; `./mvnw verify` passed after that change.
