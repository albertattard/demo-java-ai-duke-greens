# Duke Greens product vision

## Purpose

Duke Greens is a conversational green-supermarket demo for a Java stand. It shows how a Java application can combine AI with trusted business data to turn a shopper’s meal-planning intent into a practical virtual grocery basket.

The goal is not to imitate a production e-commerce service. The goal is to give a stand visitor a short, credible demonstration of AI creating business value: understanding natural-language needs, proposing suitable meals, adapting to constraints, and preparing an order from products the store can supply.

## Target experience

A visitor should be able to complete the core flow in two to three minutes:

1. They describe what they want to cook, for whom, and any relevant preferences or constraints.
2. Duke Greens asks only the follow-up questions needed to propose useful options.
3. It presents a small set of meal ideas with concise reasons they fit the request.
4. The visitor chooses meals from the presented options or requests more ideas before selection.
5. Duke Greens creates a review-only virtual basket from the required products.
6. The visitor explicitly confirms a simulated order completion.

The interface should show the conversation, selected meals, and basket throughout the journey, so the flow remains understandable to both the visitor and nearby observers.

## MVP scope

The initial MVP provides:

- A text-based, turn-oriented conversation in a web application.
- AI-assisted meal recommendations based on visitor preferences such as household size, dietary preferences, preparation time, and meal style.
- AI-generated meal options validated against a small curated product catalogue.
- Meal selection and creation of a review-only virtual basket.
- An explicit, simulated order-completion step with no payment, user account, fulfilment, or delivery.

The MVP deliberately excludes voice interaction, product-stock validation, real-time external inventory, payments, and real delivery. These exclusions protect the goal of proving the end-to-end business workflow before adding stand-specific or operational complexity.

## Product rules

- The product catalogue, package sizes, prices, and basket are authoritative application data.
- AI may interpret requests, recommend meals, and explain alternatives; it must not invent products, prices, or completed orders.
- AI-generated meal ingredients must map to products in the catalogue before a basket is shown as ready.
- The visitor must explicitly confirm the simulated order before the demo presents it as complete.
- The conversation remains channel-independent: text is the first input/output channel, while future voice input and spoken responses feed the same workflow.

## Acceptance scenario

> A visitor says: “I live alone and need three light vegetarian dinners that I can prepare in 25 minutes after work.”
>
> Duke Greens proposes three suitable AI-generated meals with structured ingredients and briefly explains their fit. The visitor can request more ideas before selecting two meal cards. Duke Greens maps the selected meals to catalogue products, creates a review-only virtual basket, and asks the visitor to confirm the virtual order. On confirmation, it displays a completed-demo order with no payment or delivery action.

## Future direction

Later iterations may add:

- Push-to-talk speech input and spoken assistant responses for the stand.
- A presenter-controlled stock dashboard to make selected products unavailable during a demonstration.
- An MCP-backed inventory capability that retrieves current availability from a live or simulated inventory service.
- Replanning and substitutions when stock changes.

These capabilities extend the same customer journey; they must not weaken the application-controlled validation of product and inventory data.
