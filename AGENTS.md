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
- Use typographic curly quotation marks and apostrophes in human-facing prose and documentation. Use straight ASCII quotes only where syntax, copying, interoperability, or code conventions require them, including code, commands, configuration, identifiers, and URLs.

## Delivery workflow

- Before starting work, verify that the working tree has no modified or untracked files. If it is not clean, stop and ask the user how to proceed.
- Before changing an existing implementation, verify that the project compiles and its tests pass. If no build or test harness exists, report that the baseline cannot yet be verified; do not represent it as passing.
- Create or update the relevant specification before implementing a behaviour change. Specifications are the source of truth: code that conflicts with an accepted specification is wrong. A specification is `Draft` while it is being prepared and `Accepted` before implementation begins. If a task or implementation relies on a draft or otherwise stale specification, stop and ask the user how to proceed.
- Create an implementation-ready task for the work, normally linked to its specification, before starting implementation.
- Deliver in small vertical slices. Identify the smallest meaningful task, start with an end-to-end test for its main flow, verify that it fails, then implement only enough to make it pass.
- Repeat that test-first vertical-slice cycle until the relevant acceptance criteria are met. Then review and refactor where warranted, and rerun the relevant verification.
- Call out tasks that do not provide direct visitor value. Such enabling work may still proceed when it is necessary, but its value and scope must be explicit.
- Use these task statuses: `Ready` for work that is specified and eligible to begin; `In progress` while implementation or verification is underway; `Review` when all required implementation and verification are complete and the task awaits user review; `Blocked` when progress cannot continue because a required external decision, access, dependency, or user input is missing; and `Completed` only after the user explicitly says the task is complete. A blocked task must record the blocker and what is needed to resume, and returns to `In progress` when resolved. Do not use `Blocked` merely because implementation or verification has failed; diagnose and address that work while the task remains `In progress`. Agents must not transition a task from `Review` to `Completed` on their own.

## Git commits

- Commit only one coherent, reviewable change at a time. Do not bulk-commit unrelated changes, including pre-existing work in the working tree.
- Write commits with a short, imperative subject and, when useful, a longer body separated by a blank line.
- Make the subject describe the change's intent, not merely the files or mechanics visible in the diff.
- Use the body to explain the motivation, constraints, and consequences that are not evident from the diff.
- Keep commit messages specific enough to be understandable when scanning Git history.

## Documentation

- Before completing work or creating a commit, review documentation affected by code or documentation changes and verify that it remains valid, accurate, and current. If it may be stale, contradictory, or invalid, stop and ask the user for direction; do not silently proceed or update it on assumption.
- Keep documentation organised by purpose beneath `docs/`; use the closest relevant directory rather than placing unrelated documents together.
- Use `docs/product/` for product vision, user journeys, and product decisions; use `docs/adr/`, `docs/specs/`, and `docs/tasks/` when their first respective artifacts are needed.
- Add a focused `README.md` when creating a documentation directory so its purpose and contents remain discoverable.
- When documentation refers to a specific repository artifact, such as a named task, specification, or ADR, use a relative Markdown link where practical. Do not add links for generic or casual mentions that do not identify a specific artifact.
- Write architecture decision records using Michael Nygard's template: Title, Status, Context, Decision, and Consequences.
