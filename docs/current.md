# Current change

## Current slice

Visitors can see, edit, and explicitly resubmit their initial meal request after a failure.

## Constraints

- Use the existing request validation and session-owned recovery route, preserving POST/Redirect/GET and safe refresh.
- Keep the single internal correction attempt, all-or-nothing validation, and hidden provider diagnostics unchanged.
- Preserve follow-up recovery and existing conversation and basket behaviour. Provider-memory rollback is outside this slice.

## Done when

- Initial recovery shows the retained request in a labelled editable textarea and explains what “Try again” submits.
- Deterministic coverage proves failure → edit → successful resubmission, and unchanged-request retry still works.
- Blank and overlong resubmissions show validation feedback and retain the attempted text without invoking the model.
- Refreshing recovery or success pages makes no model call; relevant tests and full verification pass without live OpenAI calls.

## Baseline

The working tree was clean. `./mvnw test` passed with 116 unit/MVC tests before implementation.

## Implementation and verification

Initial recovery now prefills a labelled textarea with the failed request and explains that “Try again” submits its displayed text. The retry POST validates that text through the existing service, redirects invalid edits back with feedback and their attempted text, and stores the edited request if generation fails again. Session ownership, CSRF, explicit submission, bounded internal correction, and follow-up recovery remain unchanged.

MVC coverage verifies blank and overlong edits preserve attempted text with validation feedback, and repeated generation failure retains the edited request across refreshes. Existing service tests verify those invalid inputs do not load the catalogue or call the model. Browser coverage proves initial failure → recovery refresh → edit → successful retry → result refresh, with exactly one generator call per explicit valid submission. Existing unchanged-request retry remains covered.

`PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 ./mvnw verify` passed with 118 unit/MVC tests and 27 browser tests, packaging, and licence checks, without live OpenAI calls. Chromium ran outside the sandbox for macOS process registration. A separate review using the review-current-changes skill found no material issues. Reviewed the affected specification, task references, pending work, ADR, and root README; updated recovery documentation and historical task supersession notes within this slice. Existing unrelated documentation alignment remains tracked in [pending work](pending.md).
