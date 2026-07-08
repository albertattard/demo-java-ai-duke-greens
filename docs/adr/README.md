# Architecture decision records

This directory records consequential technical decisions and their rationale. Each record focuses on one decision so later contributors can understand the trade-offs without reconstructing them from code. Records use Michael Nygard's template: Title, Status, Context, Decision, and Consequences.

## Contents

- [`ADR-0001-adopt-the-initial-technical-stack.md`](ADR-0001-adopt-the-initial-technical-stack.md) selects the MVP application, UI, data, AI, testing, and deployment stack.
- [`ADR-0002-seed-the-h2-catalogue-with-sql-and-access-it-through-jdbc.md`](ADR-0002-seed-the-h2-catalogue-with-sql-and-access-it-through-jdbc.md) chooses SQL initialization and JDBC for the fixed initial catalogue.
- [`ADR-0003-use-openai-structured-output-for-initial-meal-suggestions.md`](ADR-0003-use-openai-structured-output-for-initial-meal-suggestions.md) selects the initial OpenAI model and structured-response contract.
