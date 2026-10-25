# ADR-0003: Use OpenAI structured output for initial meal suggestions

## Status

Accepted.

## Context

[TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md) introduced Duke Greens’ first live model interaction. A visitor submits one free-form request and receives one to seven meal suggestions. That initial slice deliberately generated unconstrained meal ideas; [TASK-0005](../tasks/TASK-0005-map-meal-ingredients-to-authoritative-catalogue-products.md) subsequently evolved the response contract to receive the application-owned catalogue and map every ingredient before display.

The application needs a response that is reliable enough to render and validate without parsing prose. It must remain responsive and affordable for a stand demonstration, keep API credentials on the server, and allow tests to run without an API key or live model call.

## Decision

Use OpenAI through the Spring AI integration selected in [ADR-0001](ADR-0001-adopt-the-initial-technical-stack.md). The initial model for [TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md) was `gpt-5.4-mini`; the application default is now `gpt-6-luna`, with reasoning effort explicitly set to `none` to preserve the previous model’s default effort. Keep the existing Chat Completions endpoint and structured response contract. The model identifier is an application property and may be changed without code changes after evaluation.

Request OpenAI JSON Schema structured output for a response represented by Java records. The response has a list of meal suggestions; each suggestion has:

- name;
- preparation time in whole minutes;
- short explanation;
- servings; and
- one or more ingredients, each with an exact catalogue product slug, quantity, and unit.

The model instructions receive the visitor’s unmodified free-form request. They interpret the requested number of suggestions, default to one when no unambiguous number is present, and return no more than seven. The Java application does not parse the text to independently verify the requested count. It validates that the received response conforms to the schema, includes a non-blank assistant message, and contains zero or more complete, catalogue-mappable suggestions. Under [TASK-0007](../tasks/TASK-0007-accept-valid-responses-above-the-prompt-meal-limit.md), the seven-meal limit remains a prompt instruction only: the application accepts and presents every otherwise-valid suggestion without count-based rejection, correction, or truncation.

Java owns the model-facing interface and response validation. It sends the current catalogue names and slugs as request context, then resolves each returned slug exactly against that catalogue. It derives names, package counts, prices, and estimated costs from catalogue data. It converts timeouts, provider failures, malformed structured output, and invalid response content into the visitor-facing error state defined by the MVP specification. It never presents partial suggestions or provider error details.

The OpenAI API key is server-only configuration supplied through an environment variable or untracked local configuration. Automated tests use a deterministic fake implementation of the model-facing interface.

## Consequences

- `gpt-6-luna` is selected as a cost-conscious alternative to evaluate after live requests with `gpt-5.4-mini` repeatedly returned non-catalogue slugs. This choice does not establish improved catalogue accuracy; that remains an application-specific evaluation requirement. OpenAI lists Chat Completions and structured output as supported and permits `none` reasoning effort. [GPT-6 Luna model documentation](https://developers.openai.com/api/docs/models/gpt-6-luna)
- The response contract is explicit and testable, avoiding fragile extraction from a prose answer. Structured output enforces the response shape; application validation still checks exact catalogue membership and ingredient constraints.
- This slice can demonstrate live AI value without weakening the later rule that catalogue data, product mapping, prices, and basket totals are application-controlled.
- The model’s interpretation of a requested quantity is not independently verifiable without introducing application-side natural-language parsing. Java validates the received response’s structure and catalogue mapping without imposing an upper suggestion-count bound. A model that exceeds the prompt instruction can therefore increase response size and rendering work.
- The configured model should be evaluated against representative meal requests before a stand demonstration. If quality is insufficient, the model property can be changed without changing the response contract.
- Adding the OpenAI dependency and API-key configuration is implementation work in [TASK-0004](../tasks/TASK-0004-fulfil-a-one-shot-meal-request-with-ai-generated-suggestions.md); neither is required for the deterministic automated tests.
