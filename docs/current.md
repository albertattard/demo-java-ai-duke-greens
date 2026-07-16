# Current change

## Outcome

Visitors can continue a meal-discovery conversation on the recommendations page. The page presents the latest actionable recommendations, meals already selected for the basket, and a chronological text conversation as distinct sections. The exact layout, labels, and styling of those sections remain fluid during the slice.

## Constraints

- Keep one active conversation per visitor session, isolated by its application-generated conversation ID. Use that ID for both Spring AI chat memory and the session-held UI transcript. Starting over, completing the simulated order, and session destruction must clear both stores.
- A valid model response contains a non-blank visitor-facing assistant message and zero to seven complete meal suggestions. Zero suggestions may ask for clarification or decline an out-of-scope request; in either case the conversation remains open.
- Store each valid exchange in both the UI transcript and Spring AI memory. The transcript contains only visitor prompts and assistant messages, never recommendation cards.
- A failed provider call or invalid non-empty model response is not a conversation exchange. Show a recovery error in place of an assistant reply and prefill an editable textarea with the attempted prompt; the visitor may edit and resend it. Do not expose partial cards or store either turn until a valid response succeeds.
- Map every suggested ingredient against the application-supplied catalogue snapshot using its exact application-owned product slug. Reject an invalid non-empty response as a whole. Product data, prices, stock, basket contents, and order state remain application-controlled.
- On a follow-up, give the model the conversation history and the complete latest non-empty recommendation set. Continue the existing selected-meal context behaviour; richer prompt guidance about basket-backed meals is explicitly deferred.
- A non-empty response becomes the latest recommendation set. Display every meal in that set except a meal exactly equal to one already represented in the basket section. Do not display unselected meals from older sets. A zero-suggestion response preserves the latest non-empty set, or shows no recommendations when none exists.
- The basket section preserves the original cards for selected meals. It may therefore be the only place an exactly equal meal appears when the latest set repeats it. Keep the existing basket page and its behaviour unchanged for this slice; do not add recommendations-page removal controls yet.
- Continue to allow one-click addition of a meal to the basket. Preserve explicit confirmation for completing the simulated order.
- Remove the individual “Not for me” action; visitors express exclusions or replacements in a follow-up prompt.

## Done when

- A successful initial request adds the visitor prompt and non-blank assistant message to the conversation and displays its recommendations.
- A successful follow-up appends both turns, replaces the latest recommendation set, and leaves selected basket meals visible in their own section.
- The recommendations section contains only the latest non-empty set, excluding cards exactly equal to a basket-backed meal; it does not retain older unselected cards.
- A valid zero-suggestion response appends both turns, keeps the existing recommendations and basket unchanged, and accepts a further free-text prompt.
- A provider failure or invalid recommendation response shows editable recovery without adding a conversation turn or partial cards; an edited resend can become the next successful exchange.
- The recommendations page shows no “Not for me” action. The existing basket page remains available and unchanged.
- Starting over, successful simulated-order completion, and session destruction clear both the UI transcript and the Spring AI conversation memory.
- Conversation IDs remain isolated between sessions.

## Verification

- Baseline verification passed: `./mvnw package` completed successfully with 99 tests.
