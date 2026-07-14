# Current change

## Outcome

Give customers a concise “Capabilities and AI Approach” page that explains how Duke Greens turns AI ideas into practical Java applications without sacrificing trusted data, control, or delivery practicality.

## Constraints

- Add a public, server-rendered page with the requested headline, four capability cards, AI approach principles, Duke Greens example, help areas, and discussion call to action.
- Present Duke Greens as a concrete example, not as a generic AI showcase; do not add unsupported claims, invented metrics, or autonomous-decision promises.
- Explain that AI interprets meal requests and suggests meals; the Java application validates against the curated catalogue; visitors review the basket and explicitly confirm simulated orders.
- Use the established simple layout, palette, typography, and responsive card treatment; do not introduce a client-side framework or external assets.
- Use typographic curly quotation marks and apostrophes in human-facing prose.

## Done when

- A visitor can open “Capabilities and AI approach” from the welcome page.
- The page visibly contains the requested capabilities, approach principles, Duke Greens mapping, help areas, and a customer discussion invitation.
- Browser coverage verifies navigation and the principal content.

## Verification

- Start with a focused failing browser test for navigation and required page content.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added the public `/capabilities-and-ai-approach` page and welcome-page link. The page presents the four delivery capabilities, five trust and control principles, the Duke Greens responsibility boundary, support areas, and a focused customer discussion invitation.

Implementation verification: browser coverage was added for navigation and the principal content. `./mvnw verify` passed with 15 browser scenarios and `git diff --check` passed.
