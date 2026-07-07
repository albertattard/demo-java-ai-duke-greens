# ADR-0002: Seed the H2 catalogue with SQL and access it through JDBC

## Status

Accepted.

## Context

The landing-page product catalogue needs fixed, reviewable local product data. The MVP already uses an in-memory H2 database and does not need durable product edits, production data migrations, or ORM relationship management. Application code still needs a domain-level catalogue boundary rather than direct database access from web controllers.

## Decision

Create the H2 product table and seed its initial data from committed `schema.sql` and `data.sql` resources at application startup. Use Spring JDBC for the persistence implementation. Expose catalogue lookups through a Java repository boundary that returns domain products; controllers and later recommendation/basket workflows must not query tables directly.

Represent prices as `BigDecimal` with ISO currency code EUR. Render prices using the configured presentation locale, initially `Locale.GERMANY`.

## Consequences

- The schema and every seed price are visible and reviewable in the repository.
- The catalogue resets predictably at each application startup, which suits the stand demo and automated tests.
- No JPA or migration-tool dependency is added for this fixed read-only data set.
- Future durable catalogue management, cross-environment migrations, or more complex relationship mapping may justify revisiting this decision.
