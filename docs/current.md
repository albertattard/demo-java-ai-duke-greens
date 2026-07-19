# Current change

## Outcome

After completing a simulated order, a visitor can optionally submit a one-to-five-star rating and an optional written comment, including browser-dictated text, and receive clear confirmation that the feedback was saved.

## Constraints

- Store feedback in the existing in-memory H2 database. Feedback is intentionally lost when the application restarts; changing to a file-backed database is later work.
- A rating from one through five is required. The comment is optional and must not be displayed after submission.
- Save feedback only after the visitor explicitly submits the form. Do not auto-submit dictated text.
- Use browser-provided speech recognition where available. Do not record, upload, or retain audio, and do not introduce a speech-to-text provider.
- Keep the form one-time and available only after a completed simulated order. Preserve the existing guard against opening the thank-you page directly or refreshing it.
- Do not add AI analysis, a feedback-reporting interface, a production database, or changes to catalogue, basket, or order authority.

## Done when

- The thank-you page provides an accessible one-to-five-star rating, optional comment, and explicit “Send feedback” action.
- The comment can be dictated into its editable field, reviewed, amended, cancelled, or typed; dictation never sends feedback itself.
- A valid submission is saved to the H2 `feedback` table and shows a confirmation without echoing the comment.
- Missing or invalid ratings and oversized comments are rejected without saving feedback and with understandable recovery.
- Focused persistence, MVC, and browser coverage proves the form, validation, explicit submission, successful persistence, and dictation behaviour.

## Delivery plan

1. Feedback capture: add optional, one-time post-order feedback with a star rating, optional comment, H2 persistence, and browser dictation.
2. Persistent feedback: decide and configure a file-backed or production database with an explicit retention and operational policy.
3. Feedback insight: decide whether aggregate reporting or AI-assisted analysis is useful, including privacy, access-control, and cost constraints.

## Implementation and verification

- Baseline verified with `./mvnw verify` before implementation.
- Added an H2 `feedback` table and a JDBC repository that stores the required rating, optional comment, and capture timestamp. The configured in-memory database intentionally loses these records on application restart.
- Added a guarded, one-time feedback form to the thank-you flow. Valid submission follows POST/Redirect/GET to a one-time confirmation; invalid input retains the form without saving feedback.
- Atomically claim the session’s feedback entitlement before saving, preventing duplicate submissions from the same completed order; restore it if a database save fails so the visitor can retry.
- Reused the browser-only dictation controller for the editable comment field. It never submits feedback, records no audio, and leaves typing available when unsupported or unsuccessful.
- Added focused validation, JDBC schema/repository, MVC workflow, and browser dictation coverage.
- Refined the thank-you feedback controls with visible star choices and one responsive action row: return-to-welcome is aligned left, while dictation and submission are aligned right.
- Hide the radio inputs while retaining their keyboard and screen-reader semantics. Selecting a rating fills that star and every lower star.
- Verified with `./mvnw verify`.
