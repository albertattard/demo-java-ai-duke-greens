# Current change

## Outcome

Visitors can finish the About page by trying the Duke Greens demonstration or opening Oracle’s Value Engineering page to discuss a Java and AI opportunity, without an orphaned team-and-services page.

## Constraints

- Keep the About page focused on explaining the demonstration.
- Do not present placeholder contact details.
- Link to the established Oracle Value Engineering page rather than maintaining a Duke Greens contact page.
- Remove the unused `/lets-talk` endpoint and its implementation and tests.
- Remove the redundant `/team-and-services` page, its endpoint, dedicated tests, and CSS that is no longer used.

## Done when

- The About page ends with an invitation that supports trying the demonstration or discussing a Java and AI opportunity.
- The invitation links to `/demo`.
- A secondary “Discuss an opportunity” link opens Oracle’s Value Engineering page in a new tab.
- `/lets-talk` no longer resolves.
- `/team-and-services` no longer resolves.

## Verification

- Baseline: `./mvnw verify` passed, including 20 end-to-end tests.
- `./mvnw verify` passed, including 19 end-to-end tests.
- `git diff --check` passed.
