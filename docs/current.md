# Current change

## Outcome

Visitors can use a simplified `/about` page with clearer demo-value messaging and a consistent card-based visual treatment across the site.

## Constraints

- Replace the former `/about-this-demonstration` route with `/about`.
- Retain the responsible-AI, information, and disclaimer guidance while removing the pending-approval placeholder details.
- Apply the aligned visual treatment without changing the demo’s business behaviour.

## Done when

- The site navigation opens the `/about` page.
- The About page explains the demonstration, its responsible-AI approach, visitor information, and its illustrative nature.
- Existing visitor-facing pages use the aligned visual treatment.

## Verification

- Baseline: `./mvnw verify` passed, including 23 tests.
- `./mvnw verify` passed with the completed UI changes, including 23 tests.
