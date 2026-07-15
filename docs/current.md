# Current change

## Outcome

Visitors can understand, in concrete terms, how Duke Greens divides responsibility between AI and the application, without a separate capabilities-and-AI-approach page.

## Constraints

- Keep the About page focused on the Duke Greens demonstration; do not add consultancy or “Where we can help” messaging.
- AI may suggest meals, but the application must remain responsible for catalogue validation, basket contents, and simulated-order completion.
- Do not change the demo’s business behaviour.
- Remove the obsolete `/capabilities-and-ai-approach` page and do not leave links or routes to it.

## Done when

- The About page explains that AI suggests meals while the application validates catalogue items and controls the basket and simulated order.
- The existing responsible-AI, visitor-information, and illustrative-use guidance remains available.
- The former capabilities-and-AI-approach page, its route, its link from Let’s Talk, and dedicated tests no longer exist.

## Verification

- Baseline: `./mvnw verify` passed, including 23 end-to-end tests.
- `./mvnw verify` passed after implementation, including 22 end-to-end tests.
