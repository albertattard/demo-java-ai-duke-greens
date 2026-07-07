# Working agreements

## Challenge assumptions

- Do not accept a proposal merely because it was stated. Check that it is valid, feasible, and aligned with the demo goal.
- Call out ambiguity, missing constraints, contradictions, and untested assumptions early.
- When a decision has meaningful trade-offs, explain the evidence and the alternatives before implementing it.
- Make reasonable, low-risk assumptions only when they do not materially change the requested outcome; state those assumptions clearly.
- Ask for direction when a missing decision would materially affect scope, architecture, user experience, cost, or safety.

## Build the demo deliberately

- Keep the primary goal in view: demonstrate a credible Java application that uses AI to create business value.
- Prefer small, end-to-end vertical slices over broad, unfinished technical foundations.
- Treat product catalogue, prices, stock, and cart contents as application-controlled data. AI may recommend and explain, but must not be the source of truth for business data.
- Require explicit user confirmation before completing a simulated order or making any stateful change that is visible in the demo.
- Keep the text conversation flow independent of the input/output channel so voice can be added later without changing the business workflow.

## Quality and collaboration

- Keep changes focused and avoid unrelated refactors.
- Add or update automated tests for behaviour changes where practical.
- Run relevant verification before handing work over and report what was verified.
- Document decisions that are hard to reverse or would otherwise be surprising to future contributors.

## Git commits

- Write commits with a short, imperative subject and, when useful, a longer body separated by a blank line.
- Make the subject describe the change's intent, not merely the files or mechanics visible in the diff.
- Use the body to explain the motivation, constraints, and consequences that are not evident from the diff.
- Keep commit messages specific enough to be understandable when scanning Git history.

## Documentation

- Keep documentation organised by purpose beneath `docs/`; use the closest relevant directory rather than placing unrelated documents together.
- Use `docs/product/` for product vision, user journeys, and product decisions; use `docs/adr/`, `docs/specs/`, and `docs/tasks/` when their first respective artifacts are needed.
- Add a focused `README.md` when creating a documentation directory so its purpose and contents remain discoverable.
- Write architecture decision records using Michael Nygard's template: Title, Status, Context, Decision, and Consequences.
