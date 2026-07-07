# ADR-0001: Adopt the initial technical stack

## Status

Accepted.

## Context

Duke Greens needs a browser-based, text-first MVP that demonstrates a Java application turning a meal-planning request into a priced virtual grocery basket. The stack must support a responsive stand-demo UI, structured AI output, server-side validation, local product data, and automated testing without creating unnecessary operational dependencies.

A separate single-page application would introduce another build and deployment surface plus an API boundary before the MVP workflow is proven. Full-page server-rendered forms would avoid HTMX but make the interaction less fluid. Static JSON or YAML would be simpler than H2, but H2 better demonstrates relational business data and supports later inventory-oriented extensions without external infrastructure.

## Decision

Use the following stack for the MVP.

| Concern         | Decision                                             | Rationale                                                                                                                                                                                                           |
| --------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Java            | Java 25                                              | Current LTS release, suitable for the lifetime of the demo. [Oracle announcement](https://www.oracle.com/ca-en/news/announcement/oracle-releases-java-25-2025-09-16/)                                               |
| Build           | Maven with the Maven Wrapper                         | Familiar, widely used Java dependency management and reproducible project commands.                                                                                                                                 |
| Application     | Spring Boot modular monolith with Spring MVC         | Provides the web application, dependency management, testing support, and a simple path to a single deployable application.                                                                                         |
| UI              | Thymeleaf and HTMX                                   | Keeps rendering and business rules in the Java application while enabling partial-page updates without a separate frontend build.                                                                                   |
| Product data    | In-memory H2 database, seeded at application startup | Provides a queryable relational prototype for products, package sizes, and prices without durable state or external infrastructure.                                                                                 |
| AI integration  | Spring AI with OpenAI                                | Fits the Java/Spring application and the available OpenAI access. The model identifier remains configuration-driven rather than hard-coded. [OpenAI model guidance](https://developers.openai.com/api/docs/models/) |
| Automated tests | JUnit 5, Spring Boot Test, and Playwright for Java   | Unit and application tests verify business rules; Playwright verifies the browser journey.                                                                                                                          |
| Deployment      | Executable Spring Boot JAR                           | Keeps the stand deployment to one Java process. HTMX is served as an application asset, not fetched from a CDN.                                                                                                     |

The MVP passes the full local product catalogue to the model as context. Java validates the structured response and is the authority for product mapping, package rounding, prices, basket totals, and simulated-order completion.

- The OpenAI API key is supplied to the server through an environment variable or untracked local configuration.
- The key must never be committed, rendered into HTML, or exposed to browser-side code.
- The selected OpenAI model is an application property so capability, latency, and cost can be evaluated without changing application code.
- Tests use a fake recommendation implementation and must not require an OpenAI API key or make live model calls.

## Consequences

- The MVP is predominantly Java, remains easy to run locally and at the stand, and avoids a separate frontend build and deployment pipeline.
- The server is the single owner of visitor-session state, avoiding duplicated client-side and server-side state while HTMX keeps updates focused on the affected UI regions.
- The H2 database provides realistic relational data without external infrastructure, but it resets on application restart and is not suitable for durable production data.
- The AI integration is isolated from business validation and can be configured or replaced later; tests remain independent of live model calls.
- Interactive updates require a round trip to the server. A future feature requiring complex client-side or offline behaviour would need additional browser-side code.
- The stand requires network access to OpenAI for live recommendations, and the exact model needs empirical latency and quality testing before selecting a default.
- Voice input/output, real-time inventory, MCP, RAG, curated recipes, payments, delivery, external ordering, Spring Security, database migration tooling, durable persistence, a Node.js build pipeline, CDN dependencies, and client-side state-management remain outside this initial stack decision.
