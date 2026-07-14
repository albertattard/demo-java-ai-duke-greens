# Pending work

This document captures deferred work that is worth retaining but is not yet ready for implementation.

## Remaining conversational-shopping MVP capabilities

The following visitor-facing commitments remain from the original MVP definition. They are deferred until a future change brief makes each slice implementation-ready.

- Refine the meal-request conversation. Ask one concise follow-up question only when both dietary preference and maximum preparation time are absent. Where a preference is absent, apply the defaults of no dietary restriction, a 30-minute maximum preparation time, and a one-person household when household size is absent. The displayed meals must reflect supplied household-size, dietary, and preparation-time preferences when the catalogue supports them, and each candidate’s ingredient quantities must be for that household rather than being multiplied by servings again in the application.
- Preserve the documented suggestion-count interpretation when refining the conversation: interpret “a couple” as two and “a few” as three; let an explicit numeric count take precedence; default to one suggestion when the count is ambiguous or absent; and cap a request above seven at seven suggestions. Add deterministic tests for these rules rather than relying solely on a model instruction.
- Let the visitor request “Show more ideas” repeatedly before adding a meal to the basket. Each result must preserve the requested suggestion count and exclude every meal name shown earlier in that request.
- Let the model answer a visitor’s conversational-shopping question, or ask a clarification needed to answer it, without recommending a meal. Such informational turns must remain visibly distinct from recommendations and must not make the model authoritative for catalogue products, prices, stock, or basket contents.
- Keep the visible journey understandable to both the visitor and nearby observers by showing the conversation transcript, recommendations, selected meals, and basket throughout the flow.
- Complete the demo flow with an explicit “Complete virtual order” action, a separate confirmation action, and a visible completion state that clearly says it is simulated and has not created a payment, delivery, or external order.
- Validate that the complete core journey can be demonstrated in two to three minutes with the curated catalogue.

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
