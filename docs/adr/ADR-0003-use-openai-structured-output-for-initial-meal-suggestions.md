# ADR-0003: Use OpenAI structured output for initial meal suggestions

## Status

Accepted.

## Context

[TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md) introduced Duke Greens’ first live model interaction. A visitor submits one free-form request and receives one to seven meal suggestions. That initial slice deliberately generated unconstrained meal ideas; [TASK-0005](../tasks/TASK-0005-map-meal-ingredients-to-authoritative-catalogue-products.md) subsequently evolved the response contract to receive the application-owned catalogue and map every ingredient before display.

The application needs a response that is reliable enough to render and validate without parsing prose. It must remain responsive and affordable for a stand demonstration, keep API credentials on the server, and allow tests to run without an API key or live model call.

## Decision

Use OpenAI through the Spring AI integration selected in [ADR-0001](ADR-0001-adopt-the-initial-technical-stack.md). Configure `gpt-5.4-mini` as the default model for [TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md). The model identifier is an application property and may be changed without code changes after evaluation.

Request OpenAI JSON Schema structured output for a response represented by Java records. The response has a list of meal suggestions; each suggestion has:

- name;
- preparation time in whole minutes;
- short explanation;
- servings; and
- one or more ingredients, each with an exact catalogue product slug, quantity, and unit.

The model instructions receive the visitor’s unmodified free-form request. They interpret the requested number of suggestions, default to one when no unambiguous number is present, and return no more than seven. The Java application does not parse the text to independently verify the requested count. It validates that the received response conforms to the schema, contains one to seven complete suggestions, and is safe to present.

Java owns the model-facing interface and response validation. It sends the current catalogue names and slugs as request context, then resolves each returned slug exactly against that catalogue. It derives names, package counts, prices, and estimated costs from catalogue data. It converts timeouts, provider failures, malformed structured output, and invalid response content into the visitor-facing error state defined by the MVP specification. It never presents partial suggestions or provider error details.

The OpenAI API key is server-only configuration supplied through an environment variable or untracked local configuration. Automated tests use a deterministic fake implementation of the model-facing interface.

## Consequences

- `gpt-5.4-mini` is selected for lower latency and cost than OpenAI’s flagship models while retaining support for structured output. The current OpenAI model guide recommends smaller variants such as `gpt-5.4-mini` when latency and cost matter. [OpenAI model guide](https://developers.openai.com/api/docs/models/)
- The response contract is explicit and testable, avoiding fragile extraction from a prose answer. OpenAI lists structured outputs as a supported feature of current GPT-5 models. [OpenAI model comparison](https://developers.openai.com/api/docs/models/compare)
- This slice can demonstrate live AI value without weakening the later rule that catalogue data, product mapping, prices, and basket totals are application-controlled.
- The model’s interpretation of a requested quantity is not independently verifiable without introducing application-side natural-language parsing. Java verifies only the received response’s schema and one-to-seven size bound.
- The configured model should be evaluated against representative meal requests before a stand demonstration. If quality is insufficient, the model property can be changed without changing the response contract.
- Adding the OpenAI dependency and API-key configuration is implementation work in [TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md); neither is required for the deterministic automated tests.
