# MVP: conversational shopping flow

## Status

Draft.

## Goal

Demonstrate that Duke Greens can turn a visitor’s meal-planning request into a validated virtual grocery basket through a short text conversation.

The MVP is complete when a visitor can complete this journey reliably in two to three minutes without an account, payment, or real fulfilment.

## Scope

### Included

- A single-page, text-based web conversation for one anonymous visitor.
- Free-form requests for dinner ideas, including preferences such as household size, dietary preference, preparation time, and meal style.
- One follow-up question only when both dietary preference and maximum preparation time are absent.
- AI-generated meal suggestions, each returned with structured ingredients and quantities. The model interprets the number of suggestions requested in the visitor’s free-form text, defaults to one when no number is stated, and returns no more than seven suggestions.
- Clickable selection of one or more recommended meal cards.
- A repeatable “Show more ideas” action before selection that replaces the current suggestions with a new set of the same requested quantity.
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
4. Each suggestion shows a meal name, a short explanation of why it fits, the displayed preparation time, each mapped product’s required whole-package count, catalogue package size, and per-package price, plus an estimated standalone meal cost.
5. Before selecting a meal, the visitor may select “Show more ideas” repeatedly. Duke Greens replaces the current suggestions with new suggestions of the same requested quantity that exclude previously displayed meal names.
6. The visitor selects one or more meals by using the controls on the meal cards.
7. Duke Greens creates a virtual basket from the selected meals and maps their ingredients to catalogue products.
8. The visitor reviews the read-only basket.
9. The visitor selects “Complete virtual order.” Duke Greens asks for explicit confirmation.
10. After confirmation, Duke Greens displays a simulated-order completion state. No external transaction or fulfilment action occurs.

## Behaviour rules

### Conversation and recommendations

- The application retains the current visitor’s conversation and selected meals for the active browser session only.
- The active browser-session state is transient. Duke Greens does not persist it as a durable conversation record, and a server restart makes the active request, suggestions, selections, basket, and recovery state unavailable.
- After a visitor submits a meal request, Duke Greens performs the AI request once, retains the resulting suggestions or recovery state in the active browser session, and navigates to a GET page for that state. Refreshing or revisiting that GET page must not show a browser form-resubmission prompt or invoke the AI service again.
- “Try again” is the only action that may resubmit a failed original request. “Reset” clears the active browser-session meal-request state and returns the visitor to the initial input state.
- If a visitor opens a result or recovery URL without the required active browser-session state, Duke Greens returns the visitor to the initial request page and explains that there is no active meal request to display.
- A meal request contains from 1 through 300 characters inclusive. The application rejects a longer request before loading the catalogue or invoking the model.
- The model interprets “a couple” as two meal suggestions and “a few” as three. An explicit numeric count takes precedence. When no unambiguous count is stated, it returns one suggestion.
- The assistant generates meal candidates, but application data remains authoritative for products, prices, and package sizes.
- A meal candidate must contain a name, preparation time, short explanation, servings, and structured ingredients with quantities before it can be validated or presented.
- Each meal candidate represents one dinner for the requested household size.
- If a model response does not produce a valid catalogue-mappable suggestion set of one to seven complete candidates, the application does not retry automatically. It shows a friendly error with “Try again” and “Reset” actions rather than presenting an invalid suggestion set.
- “Try again” is a distinct visitor action that resubmits the same original request. “Reset” clears the failed request and returns the visitor to the initial input state. Provider error details and partial meal suggestions are not shown.
- The application presents from one to seven catalogue-mappable suggestions at a time. It sends the visitor’s free-form request to the model; the model returns seven suggestions when the visitor requests more than seven.
- The user can proceed with fewer selected meals than were requested.
- Reset clears the conversation, displayed options, selections, basket, and completion state for the active browser session.

### Basket

- A generated meal candidate declares its required ingredients and quantities; a product declares its package size. Each ingredient quantity is the total required to prepare that candidate for its declared servings, not a per-serving amount. The application must not multiply ingredient quantities by servings again.
- Ingredient quantities are whole integers from 1 through 99,999 inclusive. Decimal, zero, negative, values above 99,999, and non-numeric quantities make the candidate unmappable. The upper bound keeps a demonstrator-facing meal request within a plausible scale; it is not a stock, availability, or basket limit.
- Ingredient and package units use the exact lowercase values `g`, `kg`, or `ml`. Mass quantities expressed in `g` or `kg` are compatible and are converted to grams before package calculation. Volume quantities use `ml` only. Any other unit, or a mass quantity mapped to a volume package or vice versa, makes the candidate unmappable.
- The application maps candidate ingredients to sellable catalogue products and determines the quantity of each product required for the selected meals.
- Required ingredient quantities are calculated for the meal’s household size. To build the basket, the application aggregates the selected meals’ unrounded, converted ingredient quantities by product slug, then rounds each aggregate up to positive whole product packages. It does not substitute products or use ingredients left over from products outside the selected meals.
- A meal card shows each mapped product’s required whole-package count, catalogue package size, and per-package price. Its estimated cost is the sum of the packages that meal would require on its own. These estimates are informative and must not be added to calculate a basket total. The final basket calculates authoritative line prices and its total from the aggregated package quantities.
- The basket is review-only in the MVP. To alter the proposed meals, the visitor starts a new request.

### Completion

- “Complete virtual order” is available only when the basket is valid and complete for the selected meals.
- Confirmation must be a distinct visitor action after the basket review.
- Completion produces only an in-application success state labelled as simulated; it must not call a payment, delivery, or order-management system.

## Catalogue assumptions

- The initial product data set is curated and local to the application.
- The model receives the entire local product catalogue and must propose meals using only mappable ingredients.
- For each ingredient, the model returns only the catalogue product slug and the quantity and unit required by the meal. The slug must exactly match a supplied lowercase catalogue slug; the application performs no trimming, case-folding, or heuristic name matching. These are recipe requirements, not sellable-package quantities. The application resolves the slug and derives the required packages and costs from the authoritative catalogue.
- Each product has a name, price, and package size.
- A future curated recipe catalogue may be added as an additional recommendation source without changing basket product mapping.

## Acceptance criteria

The MVP satisfies this specification when all of the following are demonstrable:

1. A visitor can submit a free-form meal request and receive one to seven AI-generated meal suggestions with structured ingredients and quantities. The model fulfils requests for one to seven suggestions, defaults to one when no number is stated, and returns seven suggestions when the visitor requests more than seven.
2. The displayed options reflect supplied preferences for household size, dietary preference, and preparation time when the product catalogue supports them.
3. Selecting meals creates a basket of catalogue products with package quantities.
4. The visitor can request another set of suggestions before selection; the new set has the same requested quantity and excludes previously displayed meal names.
5. Each meal card displays package quantities, package prices, and an estimated per-meal cost; the basket displays aggregated line prices and a final total.
6. Ingredient quantities reflect the selected household size, and the basket rounds them up to whole product packages.
7. A model response that does not provide a valid one-to-seven set of complete mappable candidates produces a friendly error with “Try again” and “Reset” actions, without automatic retries.
8. Reset clears the active visitor session and returns the application to its initial state.
9. A visitor can complete a simulated order only by explicitly confirming a valid basket.
10. The completed state makes clear that no payment, delivery, or external order has been created.
11. The core journey can be demonstrated in two to three minutes using the curated data set.
12. Refreshing a meal-suggestion result or recovery page neither asks the browser to resubmit a form nor repeats an AI request. Only an explicit visitor submission, including “Try again”, may invoke the AI service.
13. The application does not promise to recover active visitor state after a server restart. A visitor whose transient state is unavailable returns to the initial request page with an accessible explanation.

## Deferred follow-on capability

Inventory-aware shopping is deliberately deferred, not discarded. A later slice will add product availability, stock validation before basket completion, unavailable-product handling, substitutions, and an optional MCP-backed inventory service. A later conversational refinement may also let visitors request replacement meal options. The product-mapping boundary in this MVP must remain suitable for adding stock validation after a basket has been built.

## Open decisions deferred to architecture and implementation

- Java and framework versions, build tool, UI technology, and deployment model.
- AI provider, model, prompt design, and structured-output mechanism.
- Whether a future durable conversation capability should retain state in a database, how long it should retain it, and how a visitor could securely recover it after losing their browser session. Such a capability is outside this MVP.
- The exact product dataset.
- Visual design, accessibility treatment, and stand-specific reset behaviour.
