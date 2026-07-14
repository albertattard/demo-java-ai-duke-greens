# Duke Greens

Duke Greens is a conversational green-supermarket demo for a Java stand. It explores how a Java application can combine AI with trusted business data to turn meal-planning requests into a validated virtual grocery basket.

The first implementation is text-based. Future iterations may add push-to-talk interaction, spoken responses, and MCP-backed inventory capabilities.

## Repository guide

- [`AGENTS.md`](AGENTS.md) contains the collaboration, quality, commit-message, and documentation conventions for contributors and agents.
- [`docs/`](docs/README.md) contains product and engineering documentation.

The project vision and MVP boundaries are in [`docs/product/vision.md`](docs/product/vision.md).

## Run locally

Duke Greens requires Java 25. The Maven Wrapper downloads the required Maven distribution automatically on its first run.

Compile the application:

```shell
./mvnw compile
```

Run the unit-test and package build (this requires neither an OpenAI API key nor any live model calls):

```shell
./mvnw package
```

## Browser-test setup

The end-to-end test suite uses Playwright-managed Chromium rather than a system-installed browser. After Maven has downloaded the project dependencies, install its required Chromium binary once for the current user:

```shell
./mvnw exec:java \
  --errors \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

The binary is cached outside the repository. Re-run this command after updating the Playwright dependency. It downloads a few hundred megabytes and requires network access.

For live meal ideas, create `~/.openai/openai-api.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: "..."
duke-greens:
  demo-access:
    access-code-hash: "a BCrypt hash from your secret manager"
```

The access-code hash is required for every profile, including `mock`. Generate and store it in a secret manager or untracked local configuration; do not use the access code itself as the property value.

Start the local application with an explicit Spring configuration import:

```shell
./mvnw \
  -Dspring-boot.run.jvmArguments="-Dspring.config.import=optional:file:${HOME}/.demo/demo-java-ai-duke-greens.yml" \
  spring-boot:run
```

Or, after packaging the application, run the executable JAR directly with the same configuration import:

```shell
java \
  -Dspring.config.import=optional:file:${HOME}/.openai/openai-api.yml \
  -jar target/duke-greens-1.0.0.jar
```

Production has no fake model: without a configured API key, its OpenAI client cannot start. The `test` profile uses a deterministic generator and a test-only key, so normal verification neither reads the home-directory secret nor calls OpenAI.

## Demo access and production safeguards

The interactive workflow at `/demo` requires one shared access code. Supply its BCrypt hash through deployment configuration; do not place the code or its hash in application configuration, source control, templates, logs, command history, or support material.

```yaml
duke-greens:
  demo-access:
    access-code-hash: "supplied-by-the-deployment-secret-store"
```

Before deploying beyond local HTTP, terminate and enforce TLS, set the session cookie’s `Secure`, `HttpOnly`, and explicitly chosen `SameSite` attributes, and configure forwarded headers safely for the chosen proxy. Apply edge rate limits to access-code attempts and AI-triggering requests. Configure OpenAI budget limits and usage alerts. The shared code protects demonstration access only; it is not a complete abuse defence.

Stop it with `Ctrl+C`.

## Offline manual testing

Start the application without OpenAI credentials or network access by using the `mock` Spring profile:

```shell
./mvnw \
  -Dspring-boot.run.jvmArguments="-Dspring.config.import=optional:file:${HOME}/.openai/openai-api.yml" \
  -Dspring-boot.run.arguments="--spring.profiles.active=mock" \
  spring-boot:run
```

The `mock` profile ignores the meal-request wording and returns one or more random catalogue-valid recommendations from a fixed local set. It is intended only for exercising the visitor flow; use the default profile for the live AI demonstration.

## Product-image resizing

Large, unserved source PNGs belong in `assets/product-images/original/`. Generate the public 300 × 225 px derivatives with ImageMagick:

```shell
brew install imagemagick
./tools/resize-product-images.sh
```

The script writes proportionally scaled, centred PNG files to `src/main/resources/static/images/300/`. It does not crop or enlarge source images.

## Live OpenAI integration check

With the local API-key file configured, run the opt-in integration check:

```shell
./mvnw verify --activate-profiles openai-integration
```

To run only the conversational-refinement test:

```shell
./mvnw verify --activate-profiles openai-integration -Dit.test=OpenAiMealSuggestionIT#refineTheRecommendation
```

It makes one request to OpenAI and verifies that the configured model returns one complete structured meal suggestion. It is excluded from normal verification because it requires credentials and network access and incurs API cost.
