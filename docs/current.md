# Current change

## Current slice

Visitors can independently select the intended meal occurrence in the basket, including when separate responses contain identical recommendation sets.

## Constraints

- Use each result set’s actual list position for basket choice keys and selected state; retain the existing `setIndex:mealIndex` identifiers.
- Preserve existing meal-selection and application-calculated basket behaviour.
- Leave duplicate-meal policy and recommendation deduplication outside this slice.

## Done when

- Deterministic regression coverage proves that identical result sets render distinct basket checkbox keys.
- Selecting the later occurrence and submitting the basket retains that selection after reload, with the earlier occurrence unselected and correct basket quantities and totals.
- Existing basket tests and the full verification suite pass without live OpenAI calls.

## Baseline

The working tree was clean before this slice. `./mvnw test` passed with 115 unit/MVC tests before implementation.

## Implementation and verification

Basket choices now use each result set’s actual list position for both the meal key and selected state. Existing identifiers, filtering, selection submission, and basket calculations remain unchanged. The corresponding defect has been removed from [pending work](pending.md); duplicate-meal policy remains deferred.

The new MVC regression reproduced the defect before the fix: two identical sets both rendered the key `0:0`. It now verifies distinct rendered keys, selects `1:0` through the basket POST, and checks the reloaded selection state, basket quantity, total, and fulfilment without invoking the model.

`PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 ./mvnw verify` passed with 116 unit/MVC tests and 26 browser tests, including packaging and licence verification, without a live OpenAI call. Browser verification required execution outside the sandbox because macOS blocked Chromium process registration inside it. A separate review using the review-current-changes skill found no material issues. Reviewed affected documentation and the root README; `git diff --check` passed.
