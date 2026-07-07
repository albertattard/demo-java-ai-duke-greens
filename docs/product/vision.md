# Duke Greens product vision

## Purpose

Duke Greens is a conversational green-supermarket demo for a Java stand. It shows how a Java application can combine AI with trusted business data to turn a shopper's meal-planning intent into a practical virtual grocery basket.

The goal is not to imitate a production e-commerce service. The goal is to give a stand visitor a short, credible demonstration of AI creating business value: understanding natural-language needs, proposing suitable meals, adapting to constraints, and preparing an order from products the store can supply.

## Target experience

A visitor should be able to complete the core flow in two to three minutes:

1. They describe what they want to cook, for whom, and any relevant preferences or constraints.
2. Duke Greens asks only the follow-up questions needed to propose useful options.
3. It presents a small set of meal ideas with concise reasons they fit the request.
4. The visitor chooses meals or asks for an adjustment.
5. Duke Greens creates an editable virtual basket from the required products.
6. The visitor explicitly confirms a simulated order completion.

The interface should show the conversation, selected meals, and basket throughout the journey, so the flow remains understandable to both the visitor and nearby observers.

## MVP scope

The initial MVP provides:

- A text-based, turn-oriented conversation in a web application.
- AI-assisted meal recommendations based on visitor preferences such as household size, dietary preferences, preparation time, and meal style.
- A small curated catalogue of recipes and purchasable products.
- Simulated stock information and deterministic validation that a basket can be supplied.
- Meal selection and creation of an editable virtual basket.
- An explicit, simulated order-completion step with no payment, user account, fulfilment, or delivery.

The MVP deliberately excludes voice interaction, real-time external inventory, payments, and real delivery. These exclusions protect the goal of proving the end-to-end business workflow before adding stand-specific or operational complexity.

## Product rules

- The product catalogue, recipe ingredients, package sizes, prices, stock status, and basket are authoritative application data.
- AI may interpret requests, recommend meals, and explain alternatives; it must not invent products, availability, prices, or completed orders.
- Before a basket is shown as ready, the application validates it against the catalogue and current stock.
- An unavailable product triggers an alternative recommendation, substitution, or clear explanation; it must not be silently included in the basket.
- The visitor must explicitly confirm the simulated order before the demo presents it as complete.
- The conversation remains channel-independent: text is the first input/output channel, while future voice input and spoken responses feed the same workflow.

## Acceptance scenario

> A visitor says: “I live alone and need three light vegetarian dinners that I can prepare in 25 minutes after work.”
>
> Duke Greens proposes three suitable meals from the available recipe catalogue and briefly explains their fit. The visitor selects two and asks for an alternative to the third. Duke Greens revises the plan, creates an editable virtual basket from the selected meals, validates the products against simulated stock, and asks the visitor to confirm the virtual order. On confirmation, it displays a completed-demo order with no payment or delivery action.

## Future direction

Later iterations may add:

- Push-to-talk speech input and spoken assistant responses for the stand.
- A presenter-controlled stock dashboard to make selected products unavailable during a demonstration.
- An MCP-backed inventory capability that retrieves current availability from a live or simulated inventory service.
- Replanning and substitutions when stock changes.

These capabilities extend the same customer journey; they must not weaken the application-controlled validation of product and inventory data.
