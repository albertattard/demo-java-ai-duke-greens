# Current change

## Outcome

Visitors can finish the About page by trying the Duke Greens demonstration or opening Oracle’s Value Engineering page to learn how to discuss a Java and AI opportunity.

## Constraints

- Keep the About page focused on explaining the demonstration.
- Do not present placeholder contact details.
- Link to the established Oracle Value Engineering page rather than maintaining a Duke Greens contact page.
- Remove the unused `/lets-talk` endpoint and its implementation and tests.

## Done when

- The About page ends with an honest invitation to try the demonstration.
- The invitation links to `/demo`.
- A secondary “Reach out” link opens Oracle’s Value Engineering page in a new tab.
- `/lets-talk` no longer resolves.

## Verification

- Baseline: `./mvnw verify` passed, including 21 end-to-end tests.
- `./mvnw verify` passed, including 20 end-to-end tests.
- `git diff --check` passed.
