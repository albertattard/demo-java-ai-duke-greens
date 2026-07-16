package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
class CheckoutController {

    private final MealRequestSession mealRequestSession;
    private final BasketPresentation basketPresentation;
    private final MealSuggestionService mealSuggestionService;

    CheckoutController(final MealRequestSession mealRequestSession, final BasketPresentation basketPresentation,
                       final MealSuggestionService mealSuggestionService) {
        this.mealRequestSession = mealRequestSession;
        this.basketPresentation = basketPresentation;
        this.mealSuggestionService = mealSuggestionService;
    }

    @GetMapping("/demo/checkout")
    String showCheckout(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        final MealRequestSessionState state = mealRequestSession.state(request);
        if (!(state instanceof SuccessfulMealRequest successfulRequest) || successfulRequest.basket().isEmpty()) {
            return mealRequestSession.initialRequestRedirect();
        }

        response.setHeader("Cache-Control", "no-store");
        basketPresentation.addTo(model, successfulRequest);
        model.addAttribute("conversationId", mealRequestSession.conversationId(request));
        return "checkout";
    }

    @PostMapping("/demo/checkout/complete")
    String completeCheckout(final HttpServletRequest request) {
        final MealRequestSessionState state = mealRequestSession.state(request);
        if (!(state instanceof SuccessfulMealRequest successfulRequest)
                || successfulRequest.basket().isEmpty()
                || !successfulRequest.basket().fulfils(successfulRequest.selectedMeals())) {
            return mealRequestSession.initialRequestRedirect();
        }

        mealSuggestionService.clearConversation(mealRequestSession.conversationId(request));
        mealRequestSession.clear(request);
        mealRequestSession.markSimulatedOrderCompleted(request);
        return "redirect:/demo/thank-you";
    }

    @GetMapping("/demo/thank-you")
    String showThankYou(final HttpServletRequest request, final HttpServletResponse response) {
        if (!mealRequestSession.consumeSimulatedOrderCompletion(request)) {
            return mealRequestSession.initialRequestRedirect();
        }

        response.setHeader("Cache-Control", "no-store");
        return "thank-you";
    }
}
