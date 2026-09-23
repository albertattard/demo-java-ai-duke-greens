# Current change

## Outcome

The end-to-end recovery test accurately proves that an invalid model response receives one internal correction attempt before the visitor sees the existing retry/reset recovery page.

## Constraints

- Change test coverage only; do not alter runtime correction, catalogue mapping, visitor flow, or provider behavior.
- Verify the initial request and exactly one corrective request separately. The correction must retain the visitor request and identify the mapper constraint that failed.
- Keep the existing assertion that no meal suggestions are displayed after a second invalid response.

## Done when

- `WelcomePageIT.showsTheRecoveryStateWithoutSuggestionsWhenTheModelReturnsAnUnknownProduct` passes with the established correction flow.
- The test proves one initial generation request and one correction request, instead of treating both as the same request.
- The relevant end-to-end and full verification suites pass.

## Implementation and verification

Updated the recovery test to stub and verify the initial and corrective requests separately. It now proves that the correction carries the failed distinct-catalogue-product constraint, while preserving the existing recovery-page assertions after the second invalid response. `./mvnw test` passes with 115 tests, and `./mvnw verify -Dit.test=WelcomePageIT` passes with 115 unit/MVC tests and 25 browser tests.
