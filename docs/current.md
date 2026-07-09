# Current change

## Outcome

Use one concise active change brief to guide implementation and independent review, without requiring a separate planning-artifact lifecycle.

## Constraints

- Keep the project’s existing safety, verification, and small-vertical-slice practices.
- Keep durable product and architecture decisions in their existing documentation.
- Retain only this active brief; replace it when the next change begins.

## Done when

- `AGENTS.md` defines the active-brief workflow and permits expected changes made during that active slice.
- The documentation index identifies this file as the active change brief.
- Documentation no longer directs new work through the retired planning-artifact workflow.

## Verification

- Run `git diff --check`.
