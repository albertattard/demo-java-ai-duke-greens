# Current change

## Outcome

Keep Duke Greens’ styling maintainable while retaining its current single stylesheet.

## Constraints

- Continue to load only `src/main/resources/static/css/application.css` for application styling.
- Do not introduce Tailwind, Bootstrap, Node-based tooling, a CSS bundler, or a CSS minification pipeline at this stage.
- When CSS maintenance is next undertaken, reduce repeated values through semantic custom properties and consolidate genuinely shared panel, card, action, form-control, and message styles within `application.css`.
- Do not split the stylesheet merely for organisation: separate source files would improve ownership but would not reduce the delivered CSS without an explicit build step.
- Preserve the current visitor-facing appearance and behaviour unless a future change brief explicitly requests a design change.

## Done when

- The application continues to use one stylesheet, `application.css`.
- Any future CSS refactoring removes duplication without adding an asset build pipeline or changing the agreed visitor experience.

## Verification

- The baseline suite passed before the refactoring: `./mvnw test` (98 tests).
- `application.css` now defines semantic design custom properties and shares the common panel, card, action, form-control, and message values without adding a build pipeline or another stylesheet.
- The full suite passed after the refactoring: `./mvnw test` (98 tests). No automated visual-regression harness exists; the refactoring preserves the prior CSS values and selectors, so browser verification remains the appropriate check for visual parity.
