# Current change

## Outcome

Let a presenter share a Duke Greens demo access code with intended visitors while keeping the public explanatory pages available and preventing unauthenticated visitors from using the AI-powered shopping workflow.

## Constraints

- Treat this as enabling work for the demonstrable shopping journey: it protects access to the AI workflow but does not itself create visitor-facing business value.
- Move every interactive shopping route beneath `/demo`, including the demo landing page, meal-request submission, recommendations, retry, reset, refinement, meal selection and dismissal, basket, checkout, completion, and thank-you routes. Do not leave an alternative root-level route that can invoke AI work, read visitor workflow state, or change basket or simulated-order state.
- Keep `/`, `/about-this-demonstration`, `/how-duke-greens-creates-value`, `/capabilities-and-ai-approach`, `/team-and-services`, and `/lets-talk` public. Keep static assets and conventional error rendering available as required for those pages.
- Require an authenticated session for `/demo` and `/demo/**`. An unauthenticated browser request must be sent to a public PIN-entry page; an unauthenticated HTMX request must receive a response that reliably moves the full browser to that page.
- Authenticate with one shared demo access code supplied only through deployment configuration. Store and compare a one-way password hash, not a plaintext PIN; do not commit, render, log, or expose the access code or its hash.
- Preserve CSRF protection for every state-changing request, including PIN submission and logout. A successful PIN entry returns the visitor to the originally requested protected page when safe; an invalid entry has a generic visitor-facing failure message.
- Provide a logout action that invalidates the HTTP session, including its retained conversation and basket state.
- Retain the existing security error rendering and CSRF behaviour for public and protected routes. Do not create a second security filter chain solely for `/demo/**`.
- Do not represent the shared access code as a complete abuse defence. Before public deployment, configure TLS, secure session-cookie attributes, edge rate limiting for PIN attempts and AI-triggering requests, and OpenAI budget and usage alerts; document the required deployment configuration without committing secrets.
- Keep visitor-facing prose free of technical security details and use typographic curly quotation marks and apostrophes.

## Done when

- A visitor can open every named public page without entering the access code, while `/demo` and all interactive descendants require it.
- The public landing page has a clear action that starts the protected demo, and existing public-page links remain valid.
- Every former interactive root-level URL either has moved beneath `/demo` or cannot perform its previous interactive function.
- A valid access code authenticates the visitor and returns them to the requested demo destination; an invalid code does not authenticate them or disclose whether any part of the code was correct.
- Protected full-page and HTMX requests made without a session consistently lead to the PIN-entry page rather than rendering an unusable fragment or leaking protected content.
- Logging out removes access and retained visitor workflow state.
- Automated MVC coverage proves the public/protected boundary, valid and invalid PIN handling, safe post-login return, logout/session invalidation, CSRF rejection, and the absence of unprotected AI-triggering or state-changing routes. Browser coverage proves the public-information-to-protected-demo journey.
- Documentation identifies the required production secret, TLS and cookie settings, edge rate limits, and OpenAI budget/usage controls without containing secret values.

## Verification

- Start with focused failing security and browser tests for the public/protected boundary and PIN flow.
- Run `./mvnw verify` and `git diff --check`.
