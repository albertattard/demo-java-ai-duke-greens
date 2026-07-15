# Current change

## Outcome

Visitors can understand, in concrete terms, how Duke Greens divides responsibility between AI and the application from the About page, without separate explanatory pages.

## Constraints

- Keep the About page focused on the Duke Greens demonstration; do not add consultancy or “Where we can help” messaging.
- AI may suggest meals, but the application must remain responsible for catalogue validation, basket contents, and simulated-order completion.
- Do not change the demo’s business behaviour.
- Remove the obsolete `/capabilities-and-ai-approach` and `/how-duke-greens-creates-value` pages and do not leave links or routes to either.

## Done when

- The About page explains that AI suggests meals while the application validates catalogue items and controls the basket and simulated order.
- The existing responsible-AI, visitor-information, and illustrative-use guidance remains available.
- The former capabilities-and-AI-approach and demo-guide pages, their routes, their links, and dedicated tests no longer exist.

## Verification

- Baseline: `./mvnw verify` passed, including 22 end-to-end tests.
- `./mvnw verify` passed after implementation, including 21 end-to-end tests.
