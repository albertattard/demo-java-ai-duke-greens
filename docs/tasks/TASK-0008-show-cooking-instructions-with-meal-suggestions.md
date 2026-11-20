# TASK-0008: Show cooking instructions with meal suggestions

## Status

Proposed. Generated instructions are the agreed approach; confirm the slice brief in [current work](../current.md) before implementation.

## Sources of truth

- [Product vision](../product/vision.md), for the conversational meal-planning journey and application-controlled business data.
- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), for recommendations, meal selection, and explicit visitor actions.
- [ADR-0003](../adr/ADR-0003-use-openai-structured-output-for-initial-meal-suggestions.md), for structured model responses and application validation.
- [Pending work](../pending.md), for the deferred recipes and preparation instructions capability that this task proposes to deliver.

## Outcome

A visitor can expand a recommended meal to read numbered cooking instructions before selecting it.

## Visitor value

Direct. Visitors can understand how to prepare a suggested meal using its listed ingredients, alongside the existing servings and preparation estimate.

## Scope

- Generate ordered cooking steps in the same structured response as each meal suggestion, for both initial requests and follow-ups. Opening instructions must not make a separate model call.
- Prompt the model to use the meal’s listed ingredients and quantities and to keep instructions consistent with its servings and preparation estimate.
- Require a non-empty list of non-blank steps for every returned meal. Carry accepted steps through catalogue mapping, session state, and meal-card presentation.
- Show steps as a numbered list in an accessible, expandable “Cooking instructions” section on the meal card. Render model text as escaped text.
- Preserve instructions with the retained meal suggestion across safe page refreshes.
- Preserve authoritative catalogue mapping, application-calculated prices and basket quantities, all-or-nothing response validation, and the existing single internal correction attempt. Missing or invalid steps use that existing validation and recovery path.
- Preserve valid assistant turns with zero suggestions; those turns require no cooking steps.

Structural validation and catalogue mapping do not establish culinary correctness or prove that free-text instructions use only the listed ingredients. Treat ingredient, quantity, serving, and timing consistency in the prose as model guidance, not an application-enforced guarantee.

## Out of scope

Curated recipe storage, recipe editing, separate instruction-generation calls, retroactive generation for retained suggestions, conversation-memory changes, and new basket or order behaviour.

## Completion criteria

1. Deterministic initial and follow-up responses with valid cooking steps display those steps on their respective meal cards.
2. A visitor can open and close “Cooking instructions” using the keyboard and read the steps in their supplied order.
3. Opening instructions and refreshing the recommendations page preserve the accepted steps without another model call.
4. Missing, empty, or blank steps are rejected through the existing bounded correction and recovery flow; no partial invalid suggestions are displayed.
5. Valid zero-suggestion guidance and existing meal selection, basket, and recovery behaviour remain supported.
6. Model-provided markup in instructions is displayed as text rather than executed or rendered as HTML.
7. Focused automated coverage and `./mvnw verify` pass without an OpenAI API key or live model calls. Mock suggestions also include usable instructions.

## Verification

Before changing implementation, verify the existing build and tests. Add focused model, mapping/service, and presentation tests for valid and invalid steps, including zero-suggestion responses and escaped output. Add browser coverage for expanding instructions and safe refresh with deterministic model-call counts. Review prompt coverage for ingredient, quantity, serving, and timing guidance; deterministic fixtures do not prove live-model culinary quality. Review affected specifications, architecture documentation, pending work, and the root README for accuracy before delivery.
