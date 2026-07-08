# TASK-0003: Present the authoritative product catalogue on the landing page

## Status

Review.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially the catalogue assumptions and basket rules.
- [Landing-page product catalogue](../specs/SPEC-0002-landing-page-product-catalogue.md).
- [ADR-0002: Seed the H2 catalogue with SQL and access it through JDBC](../adr/ADR-0002-seed-the-h2-catalogue-with-sql-and-access-it-through-jdbc.md).

## Outcome

The landing page presents cards for the local, seeded product catalogue. The catalogue remains the authoritative source of product slugs, package sizes, and prices for future recommendation validation and basket calculation.

## Visitor value

Direct. Visitors immediately see that Duke Greens is a supermarket and can inspect its available products, package sizes, and prices before using the conversational meal-planning flow.

## Scope

- Seed the initial catalogue in local application data.
- Define products with stable public slugs while retaining internal identifiers for persistence and internal relationships.
- Include each product's name, slug, package quantity and unit, and `BigDecimal` price.
- Present each seeded product as an accessible card on the landing page, showing a decorative image placeholder, its name, package quantity and unit, and formatted price.
- Add automated tests that prove the seeded catalogue is available.
- Add a browser end-to-end test that opens the landing page and asserts that product cards are visible.
- Ensure normal test runs need neither an OpenAI API key nor a live model call.

## Out of scope

- Calling OpenAI, generating recommendations, prompting, or parsing model output.
- Basket aggregation, meal selection, product stock, substitutions, payment, and order completion.
- Product search, filtering, sorting, product-detail pages, and product-selection controls.
- Stock or availability data and external product-data integrations.

## Completion criteria

1. The seeded catalogue provides product name, stable public slug, package quantity and unit, `BigDecimal` price, and EUR currency for every product displayed on the landing page.
2. Product prices use `BigDecimal`; `double` and `float` are not used for currency.
3. Automated tests cover the seeded catalogue without external credentials or network calls.
4. A browser end-to-end test demonstrates that the landing page presents the seeded products as cards with their package information and prices.
5. `./mvnw package` and `./mvnw verify` pass.

## Verification

- Start with focused failing tests for the catalogue and visible landing-page product cards.
- Run the focused test before and after implementation.
- Run `./mvnw package` and `./mvnw verify` after implementation.
- Review the root README and affected documentation for configuration or data-contract changes.

## Implementation notes

- Before implementation, the landing-page browser expectation for the `Products`
  section and product cards failed because the page did not render a catalogue.
- The focused catalogue-availability tests and browser test pass after implementation.
- `./mvnw package` and `./mvnw verify` pass with no OpenAI API key or live
  model call.
- The initial catalogue contains vegetarian pantry staples and fresh vegetables
  to support credible meal-planning demonstrations without availability claims.
- Browser assertions scope package and price text to a named product card, so
  duplicate package sizes remain unambiguous.
- Slug resolution is deliberately deferred to a later recommendation or basket
  mapping slice; this slice stores stable slugs but does not consume them.
