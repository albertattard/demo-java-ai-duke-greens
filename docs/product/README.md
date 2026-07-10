# Product documentation

This directory contains the documents that describe what Duke Greens is, who it is for, and the customer experience it must provide.

## Contents

- [`vision.md`](vision.md) defines the stand-demo purpose, MVP scope, core customer journey, business rules, acceptance scenario, and future direction.

Keep implementation details and irreversible technical decisions outside this directory: use an architecture decision record once the `docs/adr/` category exists.

## Manual demonstration criterion

The complete visitor journey, from opening the landing page to the simulated-order completion state, must be demonstrable in two to three minutes using the curated catalogue. This is a manual acceptance criterion rather than an automated test. Before claiming that it passes, record the representative request, starting state, model setup, elapsed time, and observed outcome in the active change brief or the implementation commit.

The journey must also be understandable to nearby observers. During the same manual demonstration, confirm that the visible interface makes the conversation, current meal ideas, selected meals, basket, and simulated completion state understandable without relying on the visitor’s spoken explanation. Record the observed outcome with the demonstration result.
