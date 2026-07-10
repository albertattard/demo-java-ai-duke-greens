# ADR-0004: Keep active visitor state in memory

## Status

Accepted.

## Context

Duke Greens is a short, anonymous stand demonstration. An active visitor’s meal request, recommendation results, selected meals, editable basket, and recovery state need to survive ordinary navigation and browser refreshes without being placed in URLs or browser-managed application state.

The demo does not provide accounts, durable conversations, saved baskets, order history, or a recovery mechanism after a server restart. Adding persistence would introduce data-retention, visitor-identification, operational, and security decisions that are not needed to demonstrate the current workflow.

## Decision

Keep active visitor state in a server-managed, in-memory browser session. Associate the session with the browser by a secure session cookie. Treat the state as transient: it is unavailable after a server restart, after session expiry, or when the visitor’s session is otherwise lost.

When a visitor opens a page that requires unavailable session state, return them to the initial meal-request page with an accessible explanation. Do not attempt to reconstruct the prior request, recommendations, selections, basket, or recovery state.

## Consequences

- A visitor can navigate and refresh within an active session without exposing state in URLs or duplicating it in browser-side application state.
- The application can use simple in-memory state and avoids database schema, retention, account, and recovery-workflow complexity.
- A restart or lost session ends the active journey. Visitors must begin a new meal request.
- The implementation must test missing-session handling and must not imply that a basket, recommendations, or conversation can be recovered durably.
- A future requirement for persistent baskets, conversation history, cross-device access, or recovery after restart requires a new storage, retention, identity, and security decision.
