# Current change

## Outcome

Visitors can continue a successful meal-discovery conversation from the recommendations page. A follow-up produces a new set of recommendations and the page shows a chronological text transcript of the successful visitor and assistant turns. A basket created from an earlier recommendation remains accessible after a follow-up.

## Constraints

- Add a non-blank visitor-facing assistant message to the model response contract. Treat a response without one as invalid.
- Store each successful exchange in the session-held UI transcript and in Spring AI chat memory, using the existing application-generated conversation ID.
- Replace the existing refinement flow with one generic follow-up textarea and remove the individual “Not for me” action.
- Represent successful mapped recommendations directly as an immutable, bounded list in `SuccessfulMealSuggestions` and `MealResultSet`; remove the redundant `MappedMealSuggestions` wrapper.
- After a successful follow-up, show only its latest unselected recommendations. Do not implement zero-suggestion responses, provider-failure recovery, or conversation cleanup in this slice.
- Preserve access to a non-empty basket independently of the selected recommendation card, which is not displayed after a follow-up.
- Place non-empty basket actions between the displayed recommendations and the conversation.
- Render selected meals in a separate Basket section between the recommendations and conversation; omit that section while the basket is empty.
- Product data, prices, stock, basket contents, and order state remain application-controlled.

## Done when

- A successful initial request shows the visitor prompt, a non-blank assistant message, and its recommendations.
- A successful follow-up appends both text turns in chronological order and replaces the displayed recommendations with its new set.
- After adding a meal and completing a successful follow-up, the visitor can still open the basket and see its contents.
- Basket actions appear between the recommendations and conversation sections.
- Selected meals appear only in a Basket section, which is absent before the visitor adds a meal.
- The recommendations page provides a generic follow-up form and no “Not for me” action.
- Successful exchanges are recorded in both UI state and Spring AI memory under the active conversation ID.
- No production or test code refers to `MappedMealSuggestions`; successful outcomes and stored result sets retain the one-to-seven-suggestion invariant.

## Verification

- Baseline verification before implementation: `./mvnw package` passed with 99 tests.
- Add and run a focused failing browser test for an initial request followed by a successful free-text follow-up.
- Run focused unit, MVC, and browser tests after implementation.
- Run `./mvnw package` and `./mvnw verify` before handover.
- Refactor verification: `./mvnw test -Dtest=MealSuggestionMapperTest,MealSuggestionServiceTest,BasketTest,MealRequestSessionMvcTest` passed with 50 tests; `./mvnw package` passed with 98 tests; `./mvnw verify` passed with 19 integration tests.
- Presentation correction verification: `./mvnw verify -Dit.test=WelcomePageIT` passed with 98 unit/MVC tests and 19 browser tests.
- Follow-up placement correction verification: `./mvnw verify -Dit.test=WelcomePageIT` passed with 98 unit/MVC tests and 19 browser tests.
- Conversation-card presentation verification: `./mvnw verify -Dit.test=WelcomePageIT` passed with 98 unit/MVC tests and 19 browser tests.
- Conversation-card alignment verification: `./mvnw verify -Dit.test=WelcomePageIT` passed with 98 unit/MVC tests and 19 browser tests.
- Follow-up action-row verification: `./mvnw verify -Dit.test=WelcomePageIT` passed with 98 unit/MVC tests and 19 browser tests.
- Basket continuity repair verification: `./mvnw test -Dtest=MealRequestSessionMvcTest` passed with 28 MVC tests; `./mvnw package` passed with 99 unit/MVC tests; `./mvnw verify -Dit.test=WelcomePageIT` and `./mvnw verify` each passed with 99 unit/MVC tests and 20 browser tests.
- Basket action placement verification: `./mvnw verify` passed with 99 unit/MVC tests and 20 browser tests.
- Basket section verification: `./mvnw verify` passed with 99 unit/MVC tests and 20 browser tests.
