# Pending work

This document captures deferred work that is worth retaining but is not yet ready for implementation.

## Remaining conversational-shopping MVP capabilities

The following visitor-facing commitments remain from the original MVP definition. They are deferred until a future change brief makes each slice implementation-ready.

- Ask one concise follow-up question only when both dietary preference and maximum preparation time are absent. Where a preference is absent, apply the documented defaults of no dietary restriction, a 30-minute maximum preparation time, and a one-person household when household size is absent.
- Let the visitor request “Show more ideas” repeatedly before adding a meal to the basket. Each result must preserve the requested suggestion count and exclude every meal name shown earlier in that request.
- Complete the demo flow with an explicit “Complete virtual order” action, a separate confirmation action, and a visible completion state that clearly says it is simulated and has not created a payment, delivery, or external order.
- Keep the visible journey understandable to both the visitor and nearby observers by showing the conversation, recommendations, selected meals, and basket throughout the flow.

### Completion eligibility decision

Before adding simulated order completion, decide whether a basket that no longer fulfils its selected meals may be completed. The current editable-basket behaviour deliberately treats insufficient coverage as non-blocking, while the original MVP commitment allowed completion only for a valid, complete basket. Do not implement completion until this product rule is resolved.

### Future conversation refinement

Preserve the existing request interpretation rules when refining the conversation: accept meal requests from 1 through 300 characters, interpret “a couple” as two suggestions and “a few” as three, give an explicit numeric count precedence, default to one suggestion when the count is ambiguous or absent, and cap output at seven suggestions. Continue to present only complete, catalogue-mappable candidates and show friendly recovery rather than partial results or provider details when validation fails.

## Pantry-aware meal coverage

Let a visitor record that they already have a product at home, including its quantity and unit. Use this pantry quantity with basket quantities when calculating whether selected meals can be fulfilled. Do not present an “Already have this” action until that quantity-aware coverage calculation exists.

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
