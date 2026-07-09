# Pending work

This document captures deferred work that is worth retaining but is not yet
specified or ready for implementation.

## Meal-suggestion failure metrics

Related work: [TASK-0005: Map meal ingredients to authoritative catalogue products](tasks/TASK-0005-map-meal-ingredients-to-authoritative-catalogue-products.md).

Add application metrics for meal-suggestion failures with stable categories:

- `catalogue_load_failed`
- `catalogue_empty`
- `provider_failed`
- `provider_returned_no_response`
- `meal_suggestion_unmappable`

Metrics must not include visitor requests, model responses, product names, or
exception messages. Before implementation, choose the metric registry and how
the metrics will be inspected in the demo environment.
