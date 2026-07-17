# Current change

## Outcome

Visitors who prefer to speak can dictate a meal request or conversational follow-up into the existing editable request field, then review, amend, and explicitly submit it through the existing text workflow.

## Constraints

- This slice adds dictation only. Keep spoken assistant replies as a later slice in the delivery plan.
- Use browser-provided speech recognition where available; do not record, upload, or retain audio in the application, and do not introduce a speech-to-text provider in this slice.
- Dictation is an optional input channel, not a new conversation path. A transcript fills the existing editable field and is never submitted automatically. Typed input and the existing form submission, validation, recovery, and POST/Redirect/GET behaviour remain authoritative.
- Clearly expose idle, listening, and unavailable states. The visitor can stop or cancel dictation. If permission is denied, speech recognition is unavailable, or transcription fails, retain the current field contents and provide a concise explanation without blocking typed entry or submission.
- Do not change application authority over catalogue data, prices, stock, basket contents, or order state. Preserve explicit confirmation before completing the simulated order.
- Keep speech capture isolated at the browser boundary so the text conversation workflow does not depend on the input channel and a future provider-backed implementation can replace it without changing the business workflow.

## Done when

- A visitor can begin and stop dictation from each existing request field that supports meal requests or conversational follow-ups.
- A successful dictated transcript appears in the existing editable field, can be amended, and is sent only after the visitor explicitly submits the form.
- Cancelling dictation leaves existing field text intact and sends nothing.
- Unsupported recognition, denied microphone access, and transcription errors show understandable recovery while typed entry remains available.
- Focused UI coverage proves the state transitions and confirms that dictation does not submit a request itself.

## Delivery plan

1. Dictation input: add optional browser-provided dictation that fills the existing editable request fields, with explicit submit, cancellation, and graceful unsupported/permission/error states.
2. Dictation hardening: refine partial-transcript behaviour, accessibility, and cross-browser demonstration guidance; decide whether a provider-backed speech-to-text service is justified if browser recognition is insufficient.
3. On-demand assistant read-aloud: add an explicit “Read aloud” control for the visitor-facing assistant message, with stop and replay. Do not automatically play audio.
4. Optional automatic read-aloud: only after an explicit product and accessibility decision, offer an opt-in preference for reading new assistant messages aloud.
