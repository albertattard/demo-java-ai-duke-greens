# Current change

## Outcome

A visitor whose meal-idea response contains an invalid catalogue ingredient receives valid, shop-ready meal ideas after one automatic correction attempt, without having to understand or repeat an AI formatting failure.

## Constraints

- Keep the application catalogue authoritative. Never infer, rename, or fuzzy-match a model-supplied product slug; never expose an ingredient that cannot be mapped exactly to the catalogue snapshot supplied for that request.
- Keep response mapping all-or-nothing. Do not show a partial set of suggestions when any suggestion is invalid, because the requested count and basket calculations must remain trustworthy.
- On a mapping failure only, make at most one internal corrective generation attempt. It must identify the violated response constraints and ask for a complete replacement response based on the original visitor request and the same catalogue snapshot.
- Validate the replacement response using the existing mapper. Do not weaken mapper validation or introduce automatic product substitution.
- If the correction attempt also fails, retain the existing safe failed-request recovery state and its explicit visitor actions. Do not loop, silently retry in the background, or expose provider/model diagnostics to the visitor.
- Preserve the current explicit-submission workflow, conversation isolation, and normal behaviour for provider, catalogue, and request-validation failures.
- Record concise server-side diagnostics sufficient to distinguish the first mapping failure from correction-attempt failure, without logging visitor-facing recovery details as if they were valid suggestions.

## Done when

- When an initial generated response contains an unknown catalogue slug or another mapper-detected invalid ingredient, the application sends one corrective request and displays the complete corrected response when it maps successfully.
- The corrective request states the relevant validation failure and preserves the original visitor request and catalogue snapshot.
- The application makes no more than two generation attempts for one submitted request, and a second invalid response reaches the existing retry/reset recovery page with no suggestions displayed.
- Automated service coverage proves successful correction for an invalid product slug and invalid quantity, plus safe recovery when correction fails; controller coverage proves the successful corrected result is stored and displayed normally.
- Normal successful requests still make exactly one generation call.

## Implementation and verification

Not started.
