# Current change

## Outcome

A visitor can write a follow-up of up to 1,000 characters and see the same live character counter as on the welcome page, so the conversational flow has a consistent, truthful limit and explains how to correct an overlong dictated request without losing it.

## Constraints

- Change the follow-up textarea’s browser limit from 300 to 1,000 characters; its application service already validates the shared 1,000-character maximum.
- Reuse the established accessible counter behaviour: it starts at zero, updates after typing and dictated text changes, and does not announce every keystroke. When text exceeds the maximum, it reports the current count and excess characters.
- Preserve an overlong dictated value. When a visitor submits it, prevent submission without a browser-native dialog, retain the text, focus the textarea, and announce an associated application-owned correction message.
- Keep the current conversation, dictation, and explicit follow-up submission workflow intact.

## Done when

- The follow-up textarea has a 1,000-character maximum and is associated with a visible “0 of 1,000 characters” counter.
- Browser coverage proves the initial, typed, dictated, and 1,000-character-boundary counter states.
- Browser coverage proves an overlong dictated follow-up remains available, is not submitted, and receives an accessible correction message.
- MVC coverage proves a failed follow-up redisplays the 1,000-character field and its associated counter.

## Implementation and verification

- Raised the follow-up textarea’s browser limit from 300 to 1,000 characters, matching the existing application-service validation.
- Added the established accessible character counter to the follow-up field; the shared script updates it for typing and dictation changes, including an over-limit count and excess-character message.
- Prevented overlong browser submissions with application-owned, associated alert feedback while retaining the entered text and focusing the field. The welcome and follow-up forms opt out of browser-native validation dialogs; server-side validation remains unchanged.
- Added browser coverage for the initial, typed, exact-boundary, dictated, and overlong follow-up states, including a blocked submission, retained text, and corrective feedback; MVC coverage proves a failed follow-up redisplays the field and associated counter.
- Verified with `./mvnw verify`.
