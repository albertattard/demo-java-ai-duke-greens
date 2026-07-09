# Product catalogue on the landing page

## Status

Accepted.

## Goal

Make Duke Greens recognisably a supermarket as soon as a visitor opens the application by showing the initial product catalogue as cards on the landing page.

## Scope

- The landing page retains the existing welcome message and meal-request text input.
- A visible `Products` section shows every seeded catalogue product as a card.
- Each card shows the product name, package quantity and unit, and formatted price.
- Each card includes the same decorative image placeholder. It is hidden from assistive technologies and is not derived from product data.
- Catalogue content is maintained in seeded application data and may change without a specification revision. Each product must have a stable lowercase kebab-case public slug that includes its package detail, such as `wholewheat-spaghetti-500g`.

Illustrative product records (not a fixed catalogue) are:

| Slug                        | Product              | Package | Price    |
| --------------------------- | -------------------- | ------- | -------- |
| `wholewheat-spaghetti-500g` | Wholewheat spaghetti | 500 g   | 1.49 EUR |
| `chickpeas-400g`            | Chickpeas            | 400 g   | 0.99 EUR |

## Rules

- The application owns the catalogue, prices, package quantities, and units. Product slugs are stable public references; internal identifiers remain internal.
- Prices and calculated totals use `BigDecimal`; `double` and `float` must not be used for currency.
- Duke Greens has one application-wide EUR price list. Currency is not persisted per product. The default presentation locale is Germany, so a price such as `1.49 EUR` is shown as `1,49 €`.
- Locale changes affect only presentation. Supporting another currency requires an explicitly maintained price list or conversion policy; the application does not convert catalogue prices automatically.
- Cards are display-only in this slice and do not add products to a basket or change visitor state.

## Out of scope

- Product search, filtering, sorting, categories, pagination, product-detail pages, and product-selection controls.
- Recommendations, OpenAI calls, meal selection, baskets, stock, substitutions, payment, and order completion.
- Real product images, image URLs, nutritional information, allergy claims, availability claims, and external product data.

## Acceptance criteria

1. A visitor opening the landing page sees the welcome/input experience and a visible `Products` section.
2. The section presents every seeded catalogue product as a distinct, accessible card.
3. Each card shows a decorative image placeholder, the product's name, package quantity and unit, and its EUR price using German formatting.
4. The landing page does not expose internal product identifiers or public slug values as visitor-facing product data.
5. Cards provide no product search, filtering, selection, or basket-changing action.
6. The catalogue data is available without an OpenAI API key or live model call.
