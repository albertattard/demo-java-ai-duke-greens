# Current change

## Outcome

Give customers who have explored Duke Greens a warm, plain-speaking “Let’s Talk” page that makes it easy to start an informal conversation about a focused Java and AI business opportunity.

## Constraints

- Add a public, server-rendered contact and next-steps page with the agreed introduction, starting points, lightweight engagement steps, conversation inputs, clearly labelled contact placeholders, and links back to the capabilities page and Duke Greens demo.
- Do not invent names, email addresses, links, availability, service-level commitments, credentials, customer logos, case studies, or unsupported promises.
- Keep the engagement invitation informal and pressure-free; use Java and AI to describe practical, measurable value without generic AI hype or sales jargon.
- Use the established simple layout, palette, typography, and responsive card treatment; do not introduce a client-side framework or external assets.
- Use typographic curly quotation marks and apostrophes in human-facing prose.

## Done when

- A visitor can open “Let’s Talk” from the Duke Greens demo.
- The page visibly contains every requested content section, clearly marked contact placeholders, and links to “Capabilities and AI approach” and the demo.
- Browser coverage verifies navigation, the principal content, and both destination links.

## Verification

- Start with a focused failing browser test for navigation and required page content.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added the public `/lets-talk` page and welcome-page link. The page presents the agreed starting points, lightweight engagement steps, conversation inputs, contact-detail placeholders, and links back to the capabilities page and Duke Greens demo.

Implementation verification: browser coverage was added for navigation, the principal content, and both destination links. `./mvnw verify` passed with 16 browser scenarios and `git diff --check` passed.
