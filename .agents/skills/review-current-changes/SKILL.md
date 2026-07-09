---
name: review-current-changes
description: Review current repository changes against the active change brief as a Staff Software Engineer.
---

Review the current repository changes as a Staff Software Engineer.

Read `docs/current.md` before reviewing. It is the contract for the current change: use its outcome, constraints, and acceptance checks to assess the diff. Do not infer a different objective from the user’s request. If the brief is missing or too unclear to review the changes meaningfully, say so plainly and identify the information needed.

Inspect the changed code, tests, and relevant surrounding code. Focus on correctness, architecture, security, performance, maintainability, test coverage, and production readiness. Review the changes against `docs/current.md`. Ignore minor style or formatting concerns unless they hide a real defect.

Read `docs/pending.md` before reporting findings. Do not report a finding whose underlying issue is already listed there. Report it only if the current changes introduce a distinct regression or materially increase its impact.

Provide feedback one item at a time, from most to least important. For each item, identify the affected code, explain the impact, and give a concrete recommendation. Do not invent findings; if there are no material issues, say so plainly.
