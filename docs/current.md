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

## Delivery plan

Deliver the overall outcome in the following small vertical slices. Each slice requires its own agreed concise brief before implementation; this section preserves the overall goal and does not replace it.

1. Successful conversational follow-up: visitors can submit a follow-up, see a chronological transcript, and see only the latest recommendation set. Include the response-contract change needed to capture a non-blank assistant message, one generic follow-up form in place of refinement, and successful-turn storage in both UI state and Spring AI memory. Remove “Not for me”. Defer zero-suggestion and failure recovery behaviour.
2. Selected-meal continuity: visitors retain selected meals while continuing the conversation. Render selected meal cards in their own basket section, exclude an exactly equal selected meal from the latest recommendations, preserve one-click addition, and keep the basket page unchanged. Make selected-meal identity independent of historical result-set indexes.
3. Safe conversational recovery: permit valid zero-suggestion assistant responses, preserve the latest non-empty recommendations in that case, and provide editable recovery and resend for provider or invalid-response failures. Failed attempts must not add transcript turns, cards, or Spring AI memory.
4. Conversation cleanup: clear UI transcript and Spring AI memory on start-over, successful simulated-order completion, and session destruction. Add focused coverage for cleanup and conversation-ID isolation.

## Current slice

### Outcome

Visitors can continue a meal-preparation conversation when the model returns a clarification or other guidance without meal suggestions.

### Constraints

- This is best-effort model-directed behaviour; the application does not parse visitor text to decide whether it aligns with meal preparation or needs clarification.
- A valid non-blank assistant message with zero suggestions is a conversational turn, whether initial or a follow-up. The model may use it to ask a clarification question or guide the visitor back to meal preparation.
- Preserve the boundary between model conversation and application-controlled business data. Do not make catalogue, dietary, price, stock, basket, or order claims authoritative merely because the model states them.
- Preserve the existing follow-up, recommendation, basket, and completion behaviour.

### Done when

- An initial or follow-up zero-suggestion response shows the assistant message in the transcript and permits another follow-up.
- A follow-up zero-suggestion response preserves the latest non-empty recommendations and the basket.
- A subsequent successful follow-up displays meal ideas in the same conversation.
- Focused MVC and prompt coverage proves the behaviour.

## Verification

- `mvn test` — passed: 102 tests.
- Focused MVC coverage verifies that valid zero-suggestion follow-ups retain the latest recommendations and basket while appending the assistant message to the transcript.
- Prompt coverage verifies that the model uses existing conversation context for preference-only follow-ups and asks a clarification only when the combined context is insufficient.
- OpenAI integration coverage verifies specified requests, unrelated-request guidance, and conversational refinement when run with an OpenAI credential; it was not run in this environment because no credential is configured.
