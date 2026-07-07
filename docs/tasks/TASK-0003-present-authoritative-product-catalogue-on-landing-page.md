# TASK-0003: Present the authoritative product catalogue on the landing page

## Status

Ready.

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
- Make products available to application services by stable public slugs while retaining internal identifiers for persistence and internal relationships.
- Include each product's name, slug, package quantity and unit, and `BigDecimal` price.
- Present each seeded product as an accessible card on the landing page, showing a decorative image placeholder, its name, package quantity and unit, and formatted price.
- Add automated tests that prove the seeded catalogue is available and that unknown slugs are rejected.
- Add a browser end-to-end test that opens the landing page and asserts that product cards are visible.
- Ensure normal test runs need neither an OpenAI API key nor a live model call.

## Out of scope

- Calling OpenAI, generating recommendations, prompting, or parsing model output.
- Basket aggregation, meal selection, product stock, substitutions, payment, and order completion.
- Product search, filtering, sorting, product-detail pages, and product-selection controls.
- Stock or availability data and external product-data integrations.

## Completion criteria

1. The seeded catalogue provides product name, stable public slug, package quantity and unit, `BigDecimal` price, and EUR currency for every product displayed on the landing page.
2. Application code resolves a known public slug to its product and rejects an unknown slug.
3. Product prices use `BigDecimal`; `double` and `float` are not used for currency.
4. Automated tests cover the normal and unknown-slug paths without external credentials or network calls.
5. A browser end-to-end test demonstrates that the landing page presents the seeded products as cards with their package information and prices.
6. `./mvnw package` and `./mvnw verify` pass.

## Verification

- Start with focused failing tests for cataloguing and slug resolution and for visible landing-page product cards.
- Run the focused test before and after implementation.
- Run `./mvnw package` and `./mvnw verify` after implementation.
- Review the root README and affected documentation for configuration or data-contract changes.
