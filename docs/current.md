# Current change

## Outcome

Give prospective customers a clear, customer-facing “Team and Services” page that introduces the Java Value Engineering team and explains how the Duke Greens demo relates to practical, trustworthy Java and AI delivery.

## Constraints

- Add a public, server-rendered `/team-and-services` page and make it reachable from the Duke Greens landing page.
- Use the established simple layout, colour palette, typography, and responsive card treatment; do not introduce a client-side framework or new external assets.
- Explain discovery and use-case shaping, rapid proof-of-value demos, Java application modernisation, AI integration, architecture guidance, and delivery enablement in plain customer-oriented language.
- Explain collaborative workshops, small end-to-end increments, customer-controlled business data, measurable outcomes, and knowledge transfer.
- Describe Duke Greens accurately: AI interprets natural-language needs, while the Java application retains authority over the catalogue, prices, basket contents, and order confirmation.
- Include clearly labelled placeholders only for team members’ names, roles, photographs, and contact details. Do not invent people, credentials, customer logos, metrics, case studies, or a real contact endpoint.
- Include an invitation to discuss a relevant Java or AI opportunity without making a stateful change or claiming an unsupported contact channel.
- Use typographic curly quotation marks and apostrophes in human-facing prose.

## Done when

- A visitor can navigate from the welcome page to a “Team and Services” page.
- The page has a clear headline and introduction, “What we do”, “How we work”, “Why Duke Greens”, “Meet the team”, and a clear discussion invitation.
- The service, delivery approach, and Duke Greens trust boundaries are visible and accurately worded.
- Team cards visibly mark photograph, name, role, and contact information as placeholders.
- Automated browser coverage verifies the page’s navigation and customer-facing content.

## Verification

- Start with a focused failing browser test for navigation and required page content.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added the public `/team-and-services` page, a landing-page link, customer-facing service and delivery content, Duke Greens trust-boundary explanation, and clearly labelled team placeholders.

Implementation verification: `./mvnw verify` passed after browser coverage for page navigation, required sections, the trust-boundary explanation, and team placeholders was added.
