# Current change

## Outcome

Help a visitor recover safely and confidently when they open an invalid, expired, or unavailable Duke Greens link.

## Constraints

- Use Spring Boot’s conventional `templates/error/404.html` rendering; do not add a controller just for the page.
- Keep the response free of technical details, routes, session identifiers, stack traces, and application state.
- Preserve the existing Duke Greens error-page visual language and shared stylesheet.
- Provide the agreed heading, explanation, home-page action, reassurance that the demo has no real order, payment, or delivery, and a link to the existing “How Duke Greens Creates Value” guide.
- Use semantic HTML, the title “Page not found | Duke Greens”, an `h1` suitable for UI tests, and typographic curly quotation marks and apostrophes in visitor-facing prose.

## Done when

- An invalid public URL returns a custom 404 page with the agreed visitor-facing content and no technical error details.
- The “Start a new meal plan” action returns the visitor to the Duke Greens home page.
- The page offers a link to “How Duke Greens creates value”.
- Browser coverage verifies the recovery page content and home-page action.

## Verification

- Start with a focused failing browser test for an invalid URL and recovery action.
- Run `./mvnw verify` and `git diff --check`.

Baseline verification: `./mvnw verify` passed before this slice began.

Implementation: added Spring Boot’s conventional `error/404` template, with a safe recovery message, home-page action, and a link to the value guide. The shared primary-link styling now supports a button-like action on error pages without changing the guide page’s existing appearance.

Implementation verification: browser coverage verifies the custom 404 content and its return-to-home action. MVC coverage verifies the 404 status and template title and guards against a requested-path, timestamp, trace, or exception leak. `./mvnw verify` passed with 18 browser scenarios.
