# Pending work

This document captures deferred work that is worth retaining but is not yet ready for implementation.

## Feedback from initial demo review

The following work was identified during the initial feedback review. Treat it as a backlog, not as a commitment to build a broader meal-planning product.

- Improve recommendation-failure recovery so an unsuccessful result is intelligible, retains the visitor’s request, and makes clear what “Try again” will resubmit. Add deterministic regression coverage for the reported requests: “2 balanced dinners for 3 people, requiring less than an hour to prepare”; a party menu for eight; a “Sunday roast” or beef-roast request; and an unavailable follow-up such as “I want to make fresh focaccia too.”
- Show each recommended meal’s recipe and preparation instructions.
- Let a visitor add an individual catalogue product to the basket, remove an individual basket item, and add a product again by updating its quantity rather than creating a duplicate. This must preserve application authority over basket contents and quantities. Resolve the existing [basket-editing product decision](#basket-editing-product-decision) before implementation.
- Expand the curated catalogue with essential products needed by the demonstrated requests, including salt, pepper, beef, and a suitable beef roast. Do not add drinks merely because a visitor asks about them unless drinks become an explicit demo-scope decision; the application should instead clearly state that they are not in the catalogue.
- Review and add product images for newly added catalogue products, using the supplied private-label image batch only after verifying that each asset matches its product and is suitable for the demo.
- Replace full-page refreshes with partial, reactive page updates without weakening the POST/Redirect/GET and explicit-retry guarantees described below.
- Add dictation and voice input while keeping the conversation workflow independent of its input and output channels.
- The requested “I already have olive oil” behaviour is already captured in [Pantry-aware meal coverage](#pantry-aware-meal-coverage); do not expose the action before quantity-aware coverage exists.

## Deferred conversation slices

The following agreed conversation work was moved from [the current brief](current.md) before starting voice work. It remains sequenced work, not a commitment to combine all of it into one change.

### Overall outcome

Visitors can continue a meal-discovery conversation on the recommendations page. The page presents the latest actionable recommendations, meals already selected for the basket, and a chronological text conversation as distinct sections. The exact layout, labels, and styling of those sections remain fluid during the slice.

### Constraints

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

### Overall acceptance checks

- A successful initial request adds the visitor prompt and non-blank assistant message to the conversation and displays its recommendations.
- A successful follow-up appends both turns, replaces the latest recommendation set, and leaves selected basket meals visible in their own section.
- The recommendations section contains only the latest non-empty set, excluding cards exactly equal to a basket-backed meal; it does not retain older unselected cards.
- A valid zero-suggestion response appends both turns, keeps the existing recommendations and basket unchanged, and accepts a further free-text prompt.
- A provider failure or invalid recommendation response shows editable recovery without adding a conversation turn or partial cards; an edited resend can become the next successful exchange.
- The recommendations page shows no “Not for me” action. The existing basket page remains available and unchanged.
- Starting over, successful simulated-order completion, and session destruction clear both the UI transcript and the Spring AI conversation memory.
- Conversation IDs remain isolated between sessions.

### Delivery plan

1. Successful conversational follow-up: visitors can submit a follow-up, see a chronological transcript, and see only the latest recommendation set. Include the response-contract change needed to capture a non-blank assistant message, one generic follow-up form in place of refinement, and successful-turn storage in both UI state and Spring AI memory. Remove “Not for me”. Defer zero-suggestion and failure recovery behaviour.
2. Selected-meal continuity: visitors retain selected meals while continuing the conversation. Render selected meal cards in their own basket section, exclude an exactly equal selected meal from the latest recommendations, preserve one-click addition, and keep the basket page unchanged. Make selected-meal identity independent of historical result-set indexes.
3. Safe conversational recovery: permit valid zero-suggestion assistant responses, preserve the latest non-empty recommendations in that case, and provide editable recovery and resend for provider or invalid-response failures. Failed attempts must not add transcript turns, cards, or Spring AI memory.
4. Conversation cleanup: clear UI transcript and Spring AI memory on start-over, successful simulated-order completion, and session destruction. Add focused coverage for cleanup and conversation-ID isolation.

### Next deferred slice

Visitors can continue a meal-preparation conversation when the model returns a clarification or other guidance without meal suggestions.

Constraints:

- This is best-effort model-directed behaviour; the application does not parse visitor text to decide whether it aligns with meal preparation or needs clarification.
- A valid non-blank assistant message with zero suggestions is a conversational turn, whether initial or a follow-up. The model may use it to ask a clarification question or guide the visitor back to meal preparation.
- Preserve the boundary between model conversation and application-controlled business data. Do not make catalogue, dietary, price, stock, basket, or order claims authoritative merely because the model states them.
- Preserve the existing follow-up, recommendation, basket, and completion behaviour.

Acceptance checks:

- An initial or follow-up zero-suggestion response shows the assistant message in the transcript and permits another follow-up.
- A follow-up zero-suggestion response preserves the latest non-empty recommendations and the basket.
- A subsequent successful follow-up displays meal ideas in the same conversation.
- Focused MVC and prompt coverage proves the behaviour.

The recorded verification before deferral was:

- `mvn test` — passed: 102 tests.
- Focused MVC coverage verifies that valid zero-suggestion follow-ups retain the latest recommendations and basket while appending the assistant message to the transcript.
- Prompt coverage verifies that the model uses existing conversation context for preference-only follow-ups and asks a clarification only when the combined context is insufficient.
- OpenAI integration coverage verifies specified requests, unrelated-request guidance, and conversational refinement when run with an OpenAI credential; it was not run in this environment because no credential is configured.

## Remaining conversational-shopping MVP capabilities

The following visitor-facing commitments remain from the original MVP definition. They are deferred until a future change brief makes each slice implementation-ready.

- Refine the meal-request conversation. Ask one concise follow-up question only when both dietary preference and maximum preparation time are absent. Where a preference is absent, apply the defaults of no dietary restriction, a 30-minute maximum preparation time, and a one-person household when household size is absent. The displayed meals must reflect supplied household-size, dietary, and preparation-time preferences when the catalogue supports them, and each candidate’s ingredient quantities must be for that household rather than being multiplied by servings again in the application.
- Preserve the documented suggestion-count interpretation when refining the conversation: interpret “a couple” as two and “a few” as three; let an explicit numeric count take precedence; default to one suggestion when the count is ambiguous or absent; and cap a request above seven at seven suggestions. Add deterministic tests for these rules rather than relying solely on a model instruction.
- Let the visitor request “Show more ideas” repeatedly before adding a meal to the basket. Each result must preserve the requested suggestion count and exclude every meal name shown earlier in that request.
- Let the model answer a visitor’s conversational-shopping question, or ask a clarification needed to answer it, without recommending a meal. Such informational turns must remain visibly distinct from recommendations and must not make the model authoritative for catalogue products, prices, stock, or basket contents.
- Keep the visible journey understandable to both the visitor and nearby observers by showing the conversation transcript, recommendations, selected meals, and basket throughout the flow.
- Complete the demo flow with an explicit “Complete virtual order” action, a separate confirmation action, and a visible completion state that clearly says it is simulated and has not created a payment, delivery, or external order.
- Validate that the complete core journey can be demonstrated in two to three minutes with the curated catalogue.

### Follow-up out-of-scope recovery and observability

When a visitor submits an informational follow-up such as “Are these all vegetarian meals?”, the current model instruction classifies it as out of scope. The follow-up controller currently redirects to the unchanged conversation without retaining the submitted text, showing an explanation, or writing a log entry, so the visitor experiences an apparent lost conversation. In the safe conversational-recovery slice, handle this result as a valid zero-suggestion assistant turn: preserve the latest non-empty recommendations and basket, append an explicit assistant decline or clarification to the transcript, and allow another free-text follow-up. Do not let the model make authoritative dietary-compliance claims. If answering vegetarian-status questions becomes a product requirement, introduce application-controlled dietary metadata and derive the answer from it. Add focused coverage for the POST/Redirect/GET flow, retained recommendation state, transcript outcome, and subsequent resend. Add privacy-safe observability that records the result category without logging visitor prompts or model output.

### Basket-editing product decision

Decide whether the editable basket remains the product direction or whether to restore the original review-only basket. The current change brief deliberately retains basket editing, while allowing simulated completion only when the basket fulfils the selected meals. Restoring review-only behaviour must be an explicit product decision rather than an accidental consequence of completing the original specification.

### Duplicate refinement result sets

Harden basket meal-choice keys against two structurally identical, locked refinement result sets. The current basket-page choice construction derives a result-set index by value lookup, so an identical later result set can receive the earlier set’s meal key and cannot be selected independently. Use the actual result-set position when constructing the keys, and add coverage for repeated identical refinement output. This is distinct from merely showing the same meal in two otherwise different result sets.

### Already delivered MVP behaviour to retain

Do not regress the existing request and recovery contract while implementing the remaining work: accept requests from 1 through 300 characters; validate complete catalogue-mappable candidates; present one through seven suggestions; show no partial suggestions or provider details; prevent automatic retries; make “Try again” an explicit resubmission; retain active state only in the browser session; and use POST/Redirect/GET so refreshing a result or recovery page does not repeat an AI request.

## Pantry-aware meal coverage

Let a visitor record that they already have a product at home, including its quantity and unit. Use this pantry quantity with basket quantities when calculating whether selected meals can be fulfilled. Do not present an “Already have this” action until that quantity-aware coverage calculation exists.

## Inventory-aware shopping

Add product availability and stock validation before order completion, including unavailable-product handling and substitutions. Decide whether the initial source is local demonstrator-controlled stock data or an optional MCP-backed inventory service; neither source may become authoritative for catalogue products, prices, pack sizes, or basket contents. This capability includes the previously deferred real-time external stock data and presenter-controlled stock changes.

The first inventory slice must let a presenter explicitly change a product’s demonstrator-controlled stock state and let a visitor see when an unavailable product prevents checkout. Do not begin with an administration dashboard unless the minimal presenter control cannot demonstrate the behaviour. A later slice may replan or offer substitutions using application-validated availability.

## Allergy-aware meal suggestions

Do not present an allergy filter or claim that a meal is allergy-safe until Duke Greens has authoritative per-product allergen data, including an explicit decision on cross-contamination and “may contain” information. AI must not infer allergen status from product names or recipes. Once that data exists, decide the supported allergen vocabulary, the visitor-facing safety language, and whether the feature only excludes known ingredients or can make a stronger suitability claim.

## Future recommendation sources

Evaluate adding a curated recipe catalogue as an additional recommendation source without changing the existing catalogue-product mapping boundary.

## OpenAI API key management

Retrieve the OpenAI API key from an OCI Vault secret at application startup or through a deliberately defined refresh strategy. Do not store the key in source control, application configuration, container images, or client-visible browser data. Before implementation, decide the OCI authentication mechanism, secret identifier configuration, failure behaviour when Vault is unavailable, and how the key will be supplied for local development without weakening deployed-secret handling.

## Meal-suggestion failure metrics

Add application metrics for meal-suggestion failures with stable categories:

- `catalogue_load_failed`
- `catalogue_empty`
- `provider_failed`
- `provider_returned_no_response`
- `meal_suggestion_unmappable`

Metrics must not include visitor requests, model responses, product names, or exception messages. Before implementation, choose the metric registry and how the metrics will be inspected in the demo environment.

## HTTPS and session-cookie hardening

Before the demo is deployed beyond local HTTP, define where TLS terminates, how HTTPS is enforced, and which origins require cookie access. Configure the deployed session cookie with `Secure`, `HttpOnly`, and an explicit, justified `SameSite` policy. Define safe forwarded-header handling when TLS terminates upstream, retain a deliberate local HTTP workflow, and add automated checks for the deployed cookie attributes and HTTPS enforcement.
