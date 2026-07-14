# Current change

## Outcome

Give Duke Greens customers and event visitors a clear, customer-facing “About This Demonstration” page that explains the demo’s limits, responsible AI controls, information boundaries, and the illustrative nature of meal ideas.

## Constraints

- Add a public, server-rendered page that covers the agreed demo capabilities and exclusions, responsible-AI controls, visitor-information guidance, the medical and dietary disclaimer, and clearly marked placeholders for an approved privacy notice, data-retention statement, and contact details.
- Keep catalogue products, package sizes, prices, basket contents, and simulated-order completion under application control; present AI only as a constrained and checked assistant for interpretation, meal ideas, and explanations.
- Do not invent privacy practices, retention periods, legal claims, regulatory compliance, certifications, security guarantees, or external links.
- Keep the existing simple, responsive server-rendered presentation and use typographic curly quotation marks and apostrophes in human-facing prose.

## Done when

- A visitor can open “About this demonstration” from the Duke Greens start page and return to that page.
- The page visibly contains the agreed summary, “What the demo does”, “What the demo does not do”, “Using AI responsibly”, and “Your information” sections, the three required placeholders, and the illustrative meal-idea disclaimer.
- Browser coverage verifies navigation, principal content, the placeholders, and the return link.

## Verification

- Start with a focused failing browser test for navigation and required page content.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added the public `/about-this-demonstration` page and start-page link. The page describes the demo’s scope and exclusions, the application-controlled AI safeguards, visitor information boundaries, three explicitly pending information placeholders, and the illustrative meal-idea disclaimer.

Implementation verification: focused browser coverage verifies navigation, required content, all placeholders, and the return link. `./mvnw verify -Dit.test=WelcomePageIT#explainsTheDemonstrationAndResponsibleAiUseToVisitors` passed. Full verification: `./mvnw verify` passed with 17 browser scenarios.
