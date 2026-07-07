# MVP: conversational shopping flow

## Status

Accepted.

## Goal

Demonstrate that Duke Greens can turn a visitor's meal-planning request into a validated virtual grocery basket through a short text conversation.

The MVP is complete when a visitor can complete this journey reliably in two to three minutes without an account, payment, or real fulfilment.

## Scope

### Included

- A single-page, text-based web conversation for one anonymous visitor.
- Free-form requests for dinner ideas, including preferences such as household size, dietary preference, preparation time, and meal style.
- One follow-up question only when both dietary preference and maximum preparation time are absent.
- Three AI-generated meal options, each returned with structured ingredients and quantities.
- Clickable selection of one or more recommended meal cards.
- A repeatable “Show more ideas” action before selection that replaces the current options with three new options.
- A virtual basket built from the selected recipes and matched to products in a curated catalogue.
- Per-meal estimated prices and a final basket total based on sellable product packages.
- An explicit simulated-order confirmation and a visible completion state.
- A visible reset control that clears the active visitor session.
- A visible conversation transcript, recommended meals, and basket, so the flow is understandable to the visitor and observers.

### Excluded

- Voice input, speech output, microphone handling, and audio transcription.
- User registration, sign-in, saved preferences, or order history.
- Payment, delivery scheduling, real order fulfilment, and customer notifications.
- Product stock validation, unavailable-product handling, substitutions, real-time external stock data, MCP integration, and presenter-controlled stock changes.
- Nutritional, medical, allergy-safe, or dietary-compliance guarantees.
- Products outside the curated product catalogue.

## Visitor flow

1. The visitor opens Duke Greens and sees a welcome message plus a text input.
2. The visitor describes the meals they want, for example: “I live alone and need three light vegetarian dinners I can make in 25 minutes after work.”
3. If both dietary preference and maximum preparation time are absent, Duke Greens asks one concise follow-up question for either preference. Otherwise it applies the defaults of no dietary restriction and a 30-minute maximum preparation time, where either value is absent. If household size is absent, it defaults to one person.
4. Each option shows a meal name, a short explanation of why it fits, the displayed preparation time, the mapped product packages and their prices, and an estimated per-meal cost.
5. Before selecting a meal, the visitor may select “Show more ideas” repeatedly. Duke Greens replaces the current options with three new options that exclude previously displayed meal names.
6. The visitor selects one or more meals by using the controls on the meal cards.
7. Duke Greens creates a virtual basket from the selected meals and maps their ingredients to catalogue products.
8. The visitor reviews the read-only basket.
9. The visitor selects “Complete virtual order.” Duke Greens asks for explicit confirmation.
10. After confirmation, Duke Greens displays a simulated-order completion state. No external transaction or fulfilment action occurs.

## Behaviour rules

### Conversation and recommendations

- The application retains the current visitor's conversation and selected meals for the active browser session only.
- The assistant generates meal candidates, but application data remains authoritative for products, prices, and package sizes.
- A meal candidate must contain a name, preparation time, short explanation, servings, and structured ingredients with quantities before it can be validated or presented.
- Each meal candidate represents one dinner for the requested household size.
- If a model response does not produce three catalogue-mappable candidates, the application does not retry automatically. It shows a friendly error with “Try again” and “Reset” actions rather than presenting an invalid option.
- The application presents exactly three catalogue-mappable options at a time.
- The user can proceed with fewer than three selected meals.
- Reset clears the conversation, displayed options, selections, basket, and completion state for the active browser session.

### Basket

- A generated meal candidate declares its required ingredients and quantities; a product declares its package size.
- The application maps candidate ingredients to sellable catalogue products and determines the quantity of each product required for the selected meals.
- Required ingredient quantities are calculated for the meal's household size. Basket quantities are then rounded up to positive whole product packages; the MVP does not optimise leftovers.
- A meal card's estimated cost is the sum of its required product packages. The final basket aggregates product quantities across all selected meals, then calculates the authoritative line prices and total from those aggregate package quantities.
- The basket is review-only in the MVP. To alter the proposed meals, the visitor starts a new request.

### Completion

- “Complete virtual order” is available only when the basket is valid and complete for the selected meals.
- Confirmation must be a distinct visitor action after the basket review.
- Completion produces only an in-application success state labelled as simulated; it must not call a payment, delivery, or order-management system.

## Catalogue assumptions

- The initial product data set is curated and local to the application.
- The model receives the entire local product catalogue and must propose meals using only mappable ingredients.
- Each product has a name, price, and package size.
- A future curated recipe catalogue may be added as an additional recommendation source without changing basket product mapping.

## Acceptance criteria

The MVP satisfies this specification when all of the following are demonstrable:

1. A visitor can submit a free-form meal request and receive three AI-generated meal options with structured ingredients and quantities.
2. The displayed options reflect supplied preferences for household size, dietary preference, and preparation time when the product catalogue supports them.
3. Selecting meals creates a basket of catalogue products with package quantities.
4. The visitor can request another set of three options before selection; the new set excludes previously displayed meal names.
5. Each meal card displays package quantities, package prices, and an estimated per-meal cost; the basket displays aggregated line prices and a final total.
6. Ingredient quantities reflect the selected household size, and the basket rounds them up to whole product packages.
7. A model response that does not provide three mappable candidates produces a friendly error with “Try again” and “Reset” actions, without automatic retries.
8. Reset clears the active visitor session and returns the application to its initial state.
9. A visitor can complete a simulated order only by explicitly confirming a valid basket.
10. The completed state makes clear that no payment, delivery, or external order has been created.
11. The core journey can be demonstrated in two to three minutes using the curated data set.

## Deferred follow-on capability

Inventory-aware shopping is deliberately deferred, not discarded. A later slice will add product availability, stock validation before basket completion, unavailable-product handling, substitutions, and an optional MCP-backed inventory service. A later conversational refinement may also let visitors request replacement meal options. The product-mapping boundary in this MVP must remain suitable for adding stock validation after a basket has been built.

## Open decisions deferred to architecture and implementation

- Java and framework versions, build tool, UI technology, and deployment model.
- AI provider, model, prompt design, and structured-output mechanism.
- Persistence mechanism for catalogue data and the active browser session.
- The exact product dataset.
- Visual design, accessibility treatment, and stand-specific reset behaviour.
