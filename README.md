# Duke Greens

Duke Greens is a conversational green-supermarket demo for a Java stand. It explores how a Java application can combine AI with trusted business data to turn meal-planning requests into a validated virtual grocery basket.

The first implementation is text-based. Future iterations may add push-to-talk interaction, spoken responses, and MCP-backed inventory capabilities.

## Repository guide

- [`AGENTS.md`](AGENTS.md) contains the collaboration, quality, commit-message, and documentation conventions for contributors and agents.
- [`docs/`](docs/README.md) contains product and engineering documentation.

The project vision and MVP boundaries are in [`docs/product/vision.md`](docs/product/vision.md).

## Run locally

Duke Greens requires Java 25. The Maven Wrapper downloads the required Maven
distribution automatically on its first run.

Compile the application:

```shell
./mvnw compile
```

Run the unit-test and package build (this requires neither an OpenAI API key nor
any live model calls):

```shell
./mvnw package
```

## Browser-test setup

The end-to-end test suite uses Playwright-managed Chromium rather than a
system-installed browser. After Maven has downloaded the project dependencies,
install its required Chromium binary once for the current user:

```shell
./mvnw exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

The binary is cached outside the repository. Re-run this command after updating
the Playwright dependency. It downloads a few hundred megabytes and requires
network access.

Start the local application:

```shell
./mvnw spring-boot:run
```

Stop it with `Ctrl+C`.
