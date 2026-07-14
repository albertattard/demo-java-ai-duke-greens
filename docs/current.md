# Current change

## Outcome

Give a customer seeing Duke Greens a one-minute guide to what the meal-planning demo does, why it is useful, and how responsibility is divided between AI and the Java application.

## Constraints

- Add a public, server-rendered “How Duke Greens Creates Value” page, linked from the landing page and with a prominent route back to the start of the meal-planning demo.
- Use the established simple layout, colour palette, typography, and responsive card treatment; do not introduce a client-side framework or new external assets.
- Explain the three steps: describe household, dietary, time, and meal-style needs; AI proposes and adapts meal ideas; the application validates selections against its curated catalogue and prepares a review-only basket.
- State plainly that AI recommends and explains, but is not the source of truth for products, package sizes, prices, basket contents, or completed orders; the visitor must explicitly confirm a simulated order; the demo does not take payment, arrange delivery, or create a real order.
- Describe customer value without unsupported claims: reduced effort, more relevant recommendations, faster decisions, and a credible path from intent to an actionable order.
- Include the supplied vegetarian-dinners prompt and two further concise prompts.
- Use typographic curly quotation marks and apostrophes in human-facing prose.

## Done when

- A visitor can open the guide from the welcome page and use its “Try the meal-planning demo” call to action to return to the demo start page.
- The guide contains the requested introduction, three-step walkthrough, “Why this matters”, “Trust by design”, and “What to try” sections.
- Browser coverage verifies the navigation, core guide content, trust boundaries, example prompt, and call to action.

## Verification

- Start with a focused failing browser test for navigation and required page content.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added the public `/how-duke-greens-creates-value` guide, a landing-page link, a three-step walkthrough, customer value and trust-boundary sections, example prompts, and a prominent return to the meal-planning demo.

Implementation verification: the focused browser scenario was added before the page implementation, then `./mvnw verify` passed with 14 browser scenarios and `git diff --check` passed.
