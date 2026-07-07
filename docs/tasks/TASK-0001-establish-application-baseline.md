# TASK-0001: Establish the application baseline

## Status

Completed.

## Sources of truth

- [MVP conversational shopping flow](../specs/SPEC-0001-mvp-conversational-shopping-flow.md), especially the text-based web-application scope and visitor-flow step 1.
- [ADR-0001: Adopt the initial technical stack](../adr/ADR-0001-adopt-the-initial-technical-stack.md).
- [AGENTS.md](../../AGENTS.md), particularly the delivery workflow.

## Outcome

Create a reproducible, executable Java web-application baseline that later vertical slices can test and extend.

## Visitor value

This task has no direct visitor-visible value. It is necessary enabling work: without a buildable, runnable application and automated-test foundation, the first visitor-value slice cannot be specified as a failing browser test or verified reliably.

## Scope

- Create the Maven project and Maven Wrapper using the Java, Spring Boot, Spring MVC, Thymeleaf, H2, and unit-testing choices in [ADR-0001](../adr/ADR-0001-adopt-the-initial-technical-stack.md).
- Create the minimal application entry point and configuration required for the application to start locally.
- Add unit-test dependencies and a test command that runs without an OpenAI API key or live model call.
- Document the supported commands to compile, run unit tests, and run the application in the [root README](../../README.md).

## Out of scope

- Visitor-facing pages, request input, conversation handling, catalogue data, AI integration, basket logic, and browser-journey assertions.
- Live OpenAI calls, secret configuration, deployment configuration, and any capability excluded by the MVP specification.

## Completion criteria

1. The Maven Wrapper is committed and can compile the project on a supported Java 25 environment.
2. The application starts using only local configuration and no OpenAI API key.
3. `./mvnw package` runs unit tests without network calls or credentials.
4. The [root README](../../README.md) accurately documents the compile, `package`, and run commands.

## Verification

- Run `./mvnw package`.
- Start the application using the documented local command and confirm it remains running without configuration errors.
- Review the README against the executed commands.

## Implementation notes

- Completed on 2026-07-07 with Java 25.0.3 and Spring Boot 4.1.0.
- `./mvnw package` and direct `mvn package` passed with one context-load test and no OpenAI API key or live model call.
- `./mvnw spring-boot:run` started the application successfully on port 8080 and was shut down cleanly.

## Delivery workflow exception

This is the one bootstrap exception to the normal rule that a task starts with a failing end-to-end test. No executable application or browser-test runner exists yet. The task establishes the executable application on which [TASK-0002](TASK-0002-present-welcome-and-request-input.md) will establish the browser-test capability and first browser test.
