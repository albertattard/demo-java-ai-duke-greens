# Pending work

This document captures deferred work and unresolved decisions, not a commitment to build a broader meal-planning product. Reconciled against the repository implementation and automated coverage on 24 October 2026, including the [TASK-0007](tasks/TASK-0007-accept-valid-responses-above-the-prompt-meal-limit.md) changes. It does not establish a new delivery sequence; agree each slice in [the current brief](current.md) before implementation.

## Delivered behaviour to retain

These capabilities are implemented and are no longer pending:

- Initial and follow-up requests accept non-blank text up to 1,000 characters. Typed and dictated input has a character counter; dictation is reviewed before explicit submission and typing remains available when speech recognition is unavailable. Browser coverage uses simulated recognition events, so real microphone and browser compatibility still require demonstration checks.
- A successful initial request or follow-up adds the visitor prompt and assistant message to the visible transcript. A new non-empty response supplies the latest recommendations while selected meals remain accessible separately and in the basket. The individual “Not for me” action is absent.
- A valid non-blank assistant message with zero suggestions is accepted for an initial request or follow-up. Follow-up guidance preserves the latest non-empty recommendations and basket, and the visitor can continue the conversation. These paths are covered by `MealRequestSessionMvcTest`; they are not a deferred clarification slice.
- Initial-request failure recovery displays the retained request in an editable form. “Try again” explicitly submits the displayed text; invalid edits retain the attempted text with validation feedback. Recovery and result refreshes do not invoke the model.
- A failed follow-up keeps the existing application conversation state and shows an error with the attempted prompt in an editable textarea. An edited resend is covered by MVC tests. This does not establish that failed exchanges are excluded from Spring AI memory; that gap remains below.
- Session-owned conversation IDs isolate visitor state. Start-over and simulated-order completion clear application conversation state and call the provider’s conversation cleanup; session destruction also calls cleanup. Controller/MVC and listener tests cover these paths.
- Basket meal-choice keys use actual result-set positions, so structurally identical sets retain independent selection identities. MVC regression coverage proves distinct rendered keys, selection of the later occurrence after submission and reload, and correct basket quantity and total. The product decision about deduplicating identical meals remains deferred.
- Visitors can select and remove meals, review the application-calculated basket, proceed to a separate checkout page, and explicitly complete a simulated order. Completion is blocked when the basket does not fulfil selected meals. The completion page and optional explicitly submitted feedback are implemented; no external order or payment is created.
- The system prompt requests no more than seven meals. The application accepts and displays every otherwise-valid returned meal without count-based rejection or truncation, as delivered in [TASK-0007](tasks/TASK-0007-accept-valid-responses-above-the-prompt-meal-limit.md). Deterministic coverage includes eight meals and a safe refresh.
- A response that fails catalogue mapping receives exactly one internal correction attempt using the same request and catalogue snapshot. Provider-call failures do not trigger that correction. If correction fails, the application shows recovery without partial suggestions or provider details. “Try again” remains an explicit visitor resubmission; refreshing a result or recovery GET page does not invoke the model.

The existing test suite passed with 115 unit/MVC tests and 26 browser tests during TASK-0007 verification. This documentation reconciliation did not rerun those suites or live OpenAI tests.

## Recommendation recovery and conversation gaps

- Add deterministic regression coverage for the reported requests: “2 balanced dinners for 3 people, requiring less than an hour to prepare”; a party menu for eight; a “Sunday roast” or beef-roast request; and an unavailable follow-up such as “I want to make fresh focaccia too.” Retain the bounded internal correction behaviour rather than reinstating a blanket ban on automatic correction.
- Verify and, where necessary, enforce the intended boundary that a failed provider call or invalid response adds neither turn to Spring AI memory. Existing follow-up recovery tests prove unchanged application state, not provider-memory rollback. The current memory advisor runs around the provider call before application catalogue validation; do not assume those stores advance atomically.
- Supply the complete latest non-empty recommendation set as follow-up context if that remains the intended contract. The explicit context currently contains recommendation and selected-meal names, with conversational history supplied through a 20-message Spring AI memory window. Richer guidance about selected meals remains deferred.
- Reconcile exact-meal deduplication and selection identity with the intended recommendation/basket separation. Current selection keys use result-set positions; a structurally identical meal in a later set is not automatically excluded because an earlier copy is selected. Preserve selected meals and the authoritative basket while deciding the desired behaviour.
- Retain the delivered zero-suggestion path for out-of-scope guidance. The earlier claim that an informational follow-up is silently discarded no longer describes the controller. If questions such as “Are these all vegetarian meals?” must receive authoritative answers, introduce application-controlled dietary metadata and derive those answers from it; model prose alone is insufficient.
- Add privacy-safe observability for conversational result categories without recording visitor prompts or model output. Failure metrics are specified separately below.
- Validate that the complete core journey can be demonstrated in two to three minutes with the curated catalogue. Automated flow coverage alone does not establish that timing or live-model reliability.

## Conversation product decisions

The following earlier proposals are not current implementation guarantees and need reconciliation with the accepted [MVP specification](specs/SPEC-0001-mvp-conversational-shopping-flow.md) before becoming a change brief:

- Fixed clarification rules and defaults: the earlier backlog proposed asking only when both diet and maximum preparation time are absent, with defaults of no dietary restriction, 30 minutes, and one person. The current prompt instead asks when the combined request and conversation lack enough information. Decide whether any fixed defaults are still wanted.
- Informal counts: the earlier proposal explicitly interpreted “a couple” as two and “a few” as three, with numeric counts taking precedence. The current specification leaves interpretation to the model, and the prompt defaults to one when no count is unambiguous. Decide whether to introduce a stronger contract and how to test it; do not treat prompt guidance as an application-enforced guarantee. Acceptance above seven is already delivered.
- Repeated “Show more ideas”: free-text follow-ups exist, but a dedicated action preserving the requested count and excluding every previously shown meal name remains deferred. Decide whether that action is still useful alongside the conversational form.
- Journey visibility: the recommendations page shows transcript, recommendations, and selected meals, with a separate basket and checkout. The earlier request to show all of these throughout the journey needs a concrete layout decision rather than being treated as wholly unimplemented.

## Basket-editing product decision

Decide whether meal-selection editing remains the product direction or whether to restore the original review-only basket described by the specification and [product vision](product/vision.md). Current controls edit meal selection and recalculate the basket; they do not provide arbitrary product addition, product-line removal, or direct quantity editing. Do not conflate those capabilities.

If product-level editing is retained as a future goal, let a visitor add an individual catalogue product, remove an individual basket item, and add a product again by updating its quantity rather than creating a duplicate. Preserve application authority over products and quantities, and define how edits affect selected-meal fulfilment before implementation. Pantry support is a separate quantity-aware capability below.

## Recipes, catalogue, and images

- Show each recommended meal’s recipe and preparation instructions; current meal cards display mapped ingredients and estimates, not cooking steps.
- Reassess the remaining catalogue gaps for the demonstrated requests. Salt, black pepper, beef burgers, mince, steak, and strips are already seeded. A suitable beef-roast product is not present; adding it remains deferred. Do not add drinks merely because a visitor requests them unless drinks become an explicit scope decision; clearly state when products are outside the catalogue.
- Review image coverage for remaining gaps and newly added products. The seeded beef products and black pepper already reference images; salt has no image. Use the supplied private-label image batch only after verifying that each asset matches its product and is suitable for the demo. A filename reference alone does not establish asset quality.

## Presentation, voice, and feedback

- Replace full-page navigation with partial, reactive updates without weakening POST/Redirect/GET, safe refresh, explicit visitor submission, or bounded internal correction.
- Dictation already exists for initial requests, follow-ups, and optional feedback. Further voice interaction, such as spoken assistant responses, remains deferred; keep the business workflow independent of its input and output channels.
- Decide whether visitors should see submitted feedback. Do not expose individual comments without an explicit retention, consent, moderation, and privacy policy. Feedback is stored through JDBC in the application’s local H2 database; no durable public-feedback policy is established. Consider anonymised aggregates only after those decisions are made.

## Explain Java’s role on the About page

The About page already explains AI interpretation, Java validation, application-controlled business data, and human confirmation. Add the deferred concise architecture diagram showing browser, Java application, AI provider, catalogue and basket, and the validation boundary. Keep the explanation accurate to the deployed implementation; do not imply Java is the only viable technology or add claims the demonstration does not support.

## Documentation alignment

The [MVP specification](specs/SPEC-0001-mvp-conversational-shopping-flow.md) still states a 300-character request limit, excludes voice input, describes a review-only basket, and forbids automatic retries without distinguishing internal correction. The [product vision](product/vision.md) also describes voice as future work and the basket as review-only. Reconcile these documents with delivered behaviour and the basket decision above in a separately scoped documentation change. This backlog reconciliation does not silently resolve those product decisions or modify their source documents.

## Prior OpenAI clarification regression context

Retained at the user’s earlier request as historical diagnostic context, not an active slice. `OpenAiMealSuggestionIT.handlesAnAcknowledgementOfAMealPlanClarification` already covers the reported detailed meal-plan request followed by “Yes please” in the same conversation; both responses require a non-blank assistant message and catalogue-mappable output, including valid zero-suggestion guidance. The refinement test accepts catalogue-valid chicken ingredients rather than requiring one chicken SKU. These tests remain opt-in under `openai-integration`; normal verification must not call OpenAI.

Earlier live runs observed a duplicate product slug in one suggestion and a follow-up changing servings from four to two. The focused two-turn test subsequently passed, but that did not establish stable live-suite behaviour. The first test draft also completed the visitor’s submitted sentence and incorrectly required clarification; the current test uses the submitted request and permits either response shape. The remaining work is to evaluate live reliability, including preservation of requested servings, rather than recreate the tests. No live suite was run during this reconciliation.

## Pantry-aware meal coverage

Let a visitor record that they already have a product at home, including its quantity and unit. Use this pantry quantity with basket quantities when calculating whether selected meals can be fulfilled. Do not present an “Already have this” action until that quantity-aware coverage calculation exists.

## Inventory-aware shopping

Two previously recorded starting points remain unresolved: a visitor-session availability filter before generation, and presenter-controlled stock that blocks checkout. Choose the first observable outcome before agreeing a slice; these are distinct increments, not a single implementation-ready requirement.

Add a session-scoped demo availability scenario that lets a visitor mark catalogue products available or unavailable before requesting meal ideas. The application must exclude unavailable products from the catalogue supplied to the model and reject any unavailable product returned by the model. Clearly label the setting as applying only to the visitor’s browser session and show the excluded products with the recommendations, so it demonstrates constrained AI behaviour without implying shared, real-time store inventory. Do not add quantities, reservations, stock claims, substitutions, checkout blocking, or product-preference behaviour in this slice.

Add product availability and stock validation before order completion, including unavailable-product handling and substitutions. Decide whether the initial source is local demonstrator-controlled stock data or an optional MCP-backed inventory service; neither source may become authoritative for catalogue products, prices, pack sizes, or basket contents. This capability includes the previously deferred real-time external stock data and presenter-controlled stock changes.

For the presenter-controlled checkout option, let a presenter explicitly change a product’s demonstrator-controlled stock state and let a visitor see when an unavailable product prevents checkout. Do not begin with an administration dashboard unless the minimal presenter control cannot demonstrate the behaviour. A later slice may replan or offer substitutions using application-validated availability.

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
