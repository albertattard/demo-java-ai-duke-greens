# TASK-0005: Map meal ingredients to authoritative catalogue products

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially the catalogue assumptions, basket rules, visitor-flow step 4, and acceptance criteria 2, 5, 6, and 7.
- [Product catalogue on the landing page](../specs/SPEC-0002-landing-page-product-catalogue.md), especially its catalogue-ownership and currency rules.
- [ADR-0002: Seed the H2 catalogue with SQL and access it through JDBC](../adr/ADR-0002-seed-the-h2-catalogue-with-sql-and-access-it-through-jdbc.md).
- [TASK-0004: Fulfil a one-shot meal request with AI-generated suggestions](TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md).

## Outcome

Each AI-generated meal suggestion is accepted only when every ingredient maps to a product in Duke Greens’ seeded catalogue. A displayed meal card shows the mapped product packages, their authoritative prices, and its estimated cost. Suggestions that cannot be mapped are never displayed.

## Visitor value

Direct. Visitors can see that a recommendation is grounded in products and prices controlled by Duke Greens, rather than treating model-generated ingredient text as product, stock, or pricing data.

## Scope

- Extend the model-facing structured response so every ingredient contains only one seeded product’s stable public slug plus the quantity and unit required by the meal. It must not contain a model-supplied product name, package quantity, package count, price, currency, stock state, or total.
- Supply the current authoritative catalogue product names and slugs to the model as request context. The model may recommend only product slugs supplied by the application.
- Accept meal requests from 1 through 300 characters inclusive. Reject a longer request before loading the catalogue or invoking the model. The input control exposes the same limit for immediate visitor feedback, but server-side validation remains authoritative.
- Resolve each returned slug through the application catalogue. A returned slug must exactly match a supplied lowercase catalogue slug; do not trim, case-fold, or use product-name or other heuristic matching. Do not accept a model-supplied product name, price, package size, currency, stock state, or total as authoritative.
- Assume each returned product slug identifies one applicable sellable catalogue product.
- Accept only the exact lowercase ingredient units `g`, `kg`, and `ml`. Convert compatible mass quantities between `g` and `kg` through grams before package calculation; `ml` is compatible only with `ml`. Reject any other unit or a mass/volume mismatch.
- Require ingredient product slugs to be unique within each suggestion; a duplicate slug is malformed. Ingredient quantities must be whole integers from 1 through 99,999 inclusive; reject decimal, zero, negative, values above 99,999, and non-numeric quantities. The upper bound keeps a demonstrator-facing meal request within a plausible scale; it is not a stock, availability, or basket limit. Reject the complete response if any suggestion has an unknown or duplicate slug, an invalid quantity, an incompatible unit, or cannot be fulfilled by positive whole product packages. Show the existing friendly recovery state with no partial suggestions or implementation/provider details.
- Treat each ingredient quantity as the total required for the suggestion’s declared servings; do not multiply it by servings again. Calculate each suggestion’s required packages from those quantities, rounding each required product up to a positive whole package. Use `BigDecimal` and the catalogue EUR prices to calculate the estimated standalone meal cost. This estimate is informative and must not be added to calculate a later basket total.
- Show each accepted suggestion’s mapped products accessibly: required whole-package count, catalogue package size, formatted EUR price per package, and estimated standalone meal cost. Keep the cards informational: this task adds no selection, basket, stock, availability, or order control.
- Add focused unit tests for exact slug resolution, unknown and duplicate products, the inclusive 1–99,999 quantity boundary, invalid units, package rounding, and price calculation. Add a tagged browser end-to-end test that submits a request and proves a displayed suggestion includes authoritative package and price information.
- Add focused JDBC tests that prove the H2 catalogue schema protects the application-owned product identity, slug, required fields, positive package quantities and prices, and supported package units.
- Use deterministic model fakes in automated tests. Normal Maven verification must not require credentials or a live model call.

## Out of scope

- Meal selection, cross-meal basket aggregation, basket review, “Show more ideas”, confirmation, simulated completion, stock validation, substitutions, and external product data.
- Automatic retry, follow-up questions, dietary or allergy guarantees, and changing the visitor’s original free-form request.
- Product-family mapping and commercial variant selection, including choosing between alternative brands or package sizes that could satisfy the same recipe ingredient.
- A separate unrelated-request classifier. If the model does not return catalogue-mappable dinner ideas for any reason, the normal invalid-suggestion recovery state applies.

## Completion criteria

1. The model receives the current application-owned catalogue product names and slugs. Each returned ingredient identifies a catalogue product by an exact supplied lowercase slug and provides only its recipe quantity and unit; the application derives packages and costs without trimming, case-folding, or heuristic matching.
2. The application accepts meal requests from 1 through 300 characters inclusive. It rejects longer requests without loading the catalogue or invoking the model, and the text input exposes a matching 300-character limit.
3. The application presents no suggestion unless every ingredient resolves to a current catalogue product and has a whole-integer quantity from 1 through 99,999 inclusive, is package-calculable, and has a valid unit. Decimal, zero, negative, values above 99,999, and non-numeric quantities are rejected. The only accepted units are exact lowercase `g`, `kg`, and `ml`; mass quantities convert between `g` and `kg` through grams, while `ml` is volume-only.
4. A displayed meal card shows each mapped product’s required whole-package count, catalogue package size, and German-formatted EUR price per package, plus an estimated standalone meal cost calculated from catalogue data using `BigDecimal`. The displayed estimates are not an additive basket total.
5. Package quantities are rounded up to positive whole packages for that meal; no model-supplied price, package, stock, or total is displayed or trusted.
6. An unknown or duplicate product slug, malformed quantity, incompatible unit, or otherwise unmappable response produces the existing accessible recovery state without partial suggestions or internal error details.
7. Focused unit tests cover meal-request length boundaries; exact slug resolution; unknown, differently cased, whitespace-padded, and duplicate slug rejection; the inclusive 1–99,999 quantity boundary; decimal and otherwise invalid quantity/unit rejection; rounding; and price calculation. A tagged browser end-to-end test covers request-to-mapped-suggestion display, including the required package count, package size, and per-package price.
8. `./mvnw package` and `./mvnw verify` pass without an OpenAI API key or live model call.
9. Focused JDBC tests prove the H2 catalogue schema rejects invalid product records and assigns generated product identities.

## Verification

- Add and run the focused failing unit and browser tests before implementation, including duplicate-slug rejection; retain the failure result in the implementation notes or implementation commit body.
- Run the focused unit and browser tests after implementation.
- Run the focused JDBC catalogue tests after schema-invariant coverage changes.
- Run `./mvnw package` and `./mvnw verify`.
- Review the root README and affected specifications and task documentation before handover; stop for direction if any are stale or contradicted.

## Implementation notes

- The stable product slug is the model-to-application mapping key because it is the catalogue’s public, application-controlled reference. Product names remain presentation data, not a matching heuristic.
- The existing system prompt already tells the model to respond to unrelated requests with a dinner idea rather than answer the unrelated request. This task deliberately relies on catalogue validation as the enforcement boundary instead of introducing a second intent-classification path.
- The focused mapping test was added before the mapper implementation and initially failed to compile because the mapping boundary did not exist. It now covers exact-slug resolution, duplicate and malformed ingredient rejection, mass/volume compatibility, package rounding, and authoritative price calculation.
- Mockito’s Java 25 agent is configured explicitly for Surefire and Failsafe. The browser integration test now starts reliably and proves that mapped products render their catalogue package and price information. `./mvnw clean verify` passes without an OpenAI API key or a live model call.
- The H2 product schema is protected by focused JDBC tests for generated and unique identities, slug format and uniqueness, required product fields, positive package quantities and prices, and supported package units. `mvn test` passes with this coverage.
