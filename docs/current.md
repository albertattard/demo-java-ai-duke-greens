# Current change

## Outcome

Give Duke Greens a consistent, branded page layout and a public landing page that clearly introduces the existing meal-idea-to-basket demonstration. Make the protected demo entry point self-explanatory by clarifying how to enter and obtain its access code.

## Constraints

- Use the Café Voyage layout as inspiration only: create Duke Greens branding, copy, and styling rather than reproducing its identity or text.
- Provide a shared page frame, with a responsive header containing only Home, Demo, and About, a main content area, and a two-sided footer with Java resource links, Terms of Use, and the current-year Duke Greens copyright, for public information pages and the protected demo journey.
- Keep the header, main content area, and footer aligned to one shared responsive frame width on every page.
- The landing page must state only today’s capability: visitors describe a meal idea and Duke Greens suggests meals before the application prepares a reviewable virtual basket from its catalogue.
- Do not promise structured weekly planning, course planning, budgets, allergy-aware recommendations, stock-aware recommendations, or a traditional browsing journey.
- Preserve every existing public route and the `/demo` authentication boundary. Do not change the meal-request, recommendation, basket, checkout, or simulated-order behaviour.
- Keep visitor-facing prose free of technical security details and use typographic curly quotation marks and apostrophes.
- On the demo-access page, remove the return-to-home link; provide a meaningful access-code placeholder and an accessible control that reveals or hides only the code the visitor has entered.
- Explain how to obtain an access code by providing the two contacts and their email and Slack links from the supplied Café Voyage reference, without adding its sidebar or exposing the configured access code.
- Keep the access-code input and its controls on one row while making the input comfortably wide on desktop; present the access-request guidance in its own card.

## Done when

- The public landing page presents the Duke Greens story with a clear action that starts the protected demonstration.
- Public information pages and the protected demo pages share the same branded header and footer without breaking their existing content or actions.
- Header navigation gives visitors access to Home, the protected demo, and About, while the footer provides Java resource links, Terms of Use, and the Duke Greens copyright.
- The layout remains usable on narrow viewports.
- The header, main content area, and footer share the same outer width on public, protected, and information pages.
- The landing hero’s two-line heading is visually balanced, clearly differentiated, and readable on narrow viewports.
- Automated coverage protects the landing-page and protected-demo navigation journey.
- The demo-access page has no return-to-home link, clearly prompts for an access code, lets a visitor reveal and hide their entered code, and explains how to request access.
- On desktop, the access-code input is at least 30rem wide and the access-request guidance is visually separate from the entry form.

## Verification

- Start with a failing browser test for the public landing-page-to-demo journey.
- Run `./mvnw verify` and `git diff --check`.
- Start with a failing browser test for the clarified demo-access flow.

## Delivered

- Added a central Thymeleaf Layout Dialect `layout.html` that owns the Duke Greens header, footer, document shell, and stylesheet; every public, protected, and error page now decorates it with only its page content.
- Rebuilt the public landing page as a Duke Greens-branded hero with clear demonstration and explanatory actions.
- Updated demo-return links to use the protected `/demo` journey and scoped browser-page navigation helpers to the primary navigation.
- Added browser coverage for the landing page’s header, footer, branded home link, and protected-demo action.
- Replaced the footer’s detailed-information links with the requested Java resource links, Terms of Use, and a current-year Duke Greens copyright; external links open in a new tab with opener protection.
- Added a public Terms of Use page that describes the demonstration without making unverified data-retention claims, and browser coverage that follows the footer link to it.
- Unified the header, main-content, and footer frame around shared width and horizontal-padding values, removing page-specific width caps that broke alignment.
- Added browser coverage that verifies the shared frame width on public, protected, and information pages.
- Styled the landing hero’s second heading as a compact supporting line with subtle rules, preserving a clear hierarchy below “Java & AI” and readability at narrow widths.
- Replaced the demo-access return-to-home link with an access-code field that has a clear placeholder and an accessible reveal/hide control for the visitor’s entered code; it does not expose the configured access code.
- Added a compact “Need access?” section with the requested Albert Attard and Jae Hahn email and Slack contact options, without a sidebar.
- Added browser coverage for the access-code placeholder, reveal/hide interaction, access-request contacts, and absence of the removed return link.
- Widened the desktop access-code input to 30rem while preserving a one-row form and a narrow-viewport fallback, and restyled the access-request guidance as a separate card.
- Extended browser coverage to enforce the desktop input width.
- Corrected the active landing-page test to expect its delivered “Try the demo” action.
- Verified with `./mvnw clean verify` and `git diff --check`.
