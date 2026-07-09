---
name: review-current-changes
description: Review current repository changes for a named task as a Staff Software Engineer.
---

Review the current code changes made to implement the task reference provided in the user’s request as a Staff Software Engineer.

Inspect the changed code, tests, and relevant surrounding code. Focus on correctness, architecture, security, performance, maintainability, test coverage, and production readiness. Ignore minor style or formatting concerns unless they hide a real defect.

Read `docs/pending.md` before reporting findings. Do not report a finding whose underlying issue is already listed there. Report it only if the current changes introduce a distinct regression or materially increase its impact.

Provide feedback one item at a time, from most to least important. For each item, identify the affected code, explain the impact, and give a concrete recommendation. Do not invent findings; if there are no material issues, say so plainly.
