# Pending work

This document captures deferred work that is worth retaining but is not yet ready for implementation.

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
