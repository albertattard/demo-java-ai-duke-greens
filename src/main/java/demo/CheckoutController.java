package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
class CheckoutController {

    private final MealRequestSession mealRequestSession;
    private final BasketPresentation basketPresentation;

    CheckoutController(final MealRequestSession mealRequestSession, final BasketPresentation basketPresentation) {
        this.mealRequestSession = mealRequestSession;
        this.basketPresentation = basketPresentation;
    }

    @GetMapping("/checkout")
    String showCheckout(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        final MealRequestSessionState state = mealRequestSession.state(request);
        if (!(state instanceof SuccessfulMealRequest successfulRequest) || successfulRequest.basket().isEmpty()) {
            return mealRequestSession.initialRequestRedirect();
        }

        response.setHeader("Cache-Control", "no-store");
        basketPresentation.addTo(model, successfulRequest);
        return "checkout";
    }
}
