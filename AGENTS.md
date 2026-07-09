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

- Before beginning a new vertical slice, verify that the working tree has no modified or untracked files. After recording its brief in `docs/current.md`, permit expected changes to that file and to files belonging to the same active slice. If changes predate the slice or are unrelated to it, stop and ask the user how to proceed. Read-only inspection may proceed, but must report existing changes and must not alter them without direction.
- Before changing an existing implementation, verify that the project compiles and its tests pass. If no build or test harness exists, report that the baseline cannot yet be verified; do not represent it as passing.
- Use `docs/current.md` as the single shared contract for the current change. Do not create a second current-work file.
- Before implementation, agree and record a concise change brief in `docs/current.md`: the visitor outcome, non-negotiable constraints, and observable acceptance checks. Record only information needed to guide implementation and review.
- After the user has agreed the brief, the model may update `docs/current.md` without further approval only to record that agreed brief and factual implementation or verification results. It must ask before changing the outcome, constraints, or acceptance checks, or before replacing the brief for a new vertical slice.
- Deliver in small vertical slices. Identify the smallest meaningful visitor-facing change, start with an end-to-end test for its main flow, verify that it fails, then implement only enough to make it pass.
- Call out enabling work that does not provide direct visitor value. It may proceed when necessary, but its value and scope must be explicit in `docs/current.md`.
- After implementation, update `docs/current.md` only when needed to accurately describe what was delivered, the tests added or updated, and the verification run. Do not turn it into a historical project log.
- Review each completed slice in a separate session where practical. Provide the reviewer with `docs/current.md`, the diff, and any relevant durable documentation. Ask it to assess the change against the brief and identify concrete correctness, safety, user-experience, design, and test gaps without inventing requirements outside that contract.
- Address relevant review findings, rerun the appropriate verification, and keep `docs/current.md` focused on the next change. A new brief replaces the prior one; the repository retains only one `docs/current.md`.

## Git commits

- Commit only one coherent, reviewable change at a time. Do not bulk-commit unrelated changes, including pre-existing work in the working tree.
- Write commits with a short, imperative subject and, when useful, a longer body separated by a blank line.
- Make the subject describe the change's intent, not merely the files or mechanics visible in the diff.
- Use the body to explain the motivation, constraints, and consequences that are not evident from the diff.
- Keep commit messages specific enough to be understandable when scanning Git history.

## Documentation

- Before completing work or creating a commit, review documentation affected by code or documentation changes and verify that it remains valid, accurate, and current. If accuracy depends on an unresolved decision, stop and ask the user for direction; do not silently proceed or update it on assumption. When documentation maintenance is explicitly requested, update it within the requested scope.
- Keep documentation organised by purpose beneath `docs/`; use the closest relevant directory rather than placing unrelated documents together.
- Use `docs/product/` for product vision, user journeys, and product decisions, and `docs/adr/` for durable architecture decisions.
- Add a focused `README.md` when creating a documentation directory so its purpose and contents remain discoverable.
- When documentation refers to a specific repository artifact, such as `docs/current.md` or an ADR, use a relative Markdown link where practical. Do not add links for generic or casual mentions that do not identify a specific artifact.
- Write Markdown prose as one logical paragraph per source line; do not hard-wrap prose. Preserve deliberate line breaks for lists, tables, blockquotes, code blocks, and other Markdown structures.
- Write architecture decision records using Michael Nygard's template: Title, Status, Context, Decision, and Consequences.
