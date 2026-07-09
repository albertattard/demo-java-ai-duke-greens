package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import module java.base;

@Controller
class WelcomePageController {

    private static final String MEAL_REQUEST_STATE = "mealRequestState";
    private static final String NO_ACTIVE_MEAL_REQUEST = "There is no active meal request to display.";
    private static final String NO_ACTIVE_MEAL_REQUEST_NOTICE = "no-active-meal-request";

    private final ProductCatalogue productCatalogue;
    private final MealSuggestionService mealSuggestionService;

    WelcomePageController(
            final ProductCatalogue productCatalogue,
            final MealSuggestionService mealSuggestionService) {
        this.productCatalogue = productCatalogue;
        this.mealSuggestionService = mealSuggestionService;
    }

    @GetMapping("/")
    String showWelcomePage(@RequestParam(required = false) final String notice, final Model model) {
        model.addAttribute("products", allProducts());
        if (NO_ACTIVE_MEAL_REQUEST_NOTICE.equals(notice)) {
            model.addAttribute("informationMessage", NO_ACTIVE_MEAL_REQUEST);
        }
        return "welcome";
    }

    @PostMapping("/meal-request")
    String submitMealRequest(
            @RequestParam(required = false) final String mealRequest,
            final HttpServletRequest request,
            final Model model) {
        final MealRequestResult result = mealSuggestionService.submit(mealRequest);

        if (result instanceof InvalidRequest(final String message)) {
            model.addAttribute("products", allProducts());
            model.addAttribute("validationMessage", message);
            return "welcome";
        }

        return storeResultAndRedirect(mealRequest, result, request.getSession());
    }

    @GetMapping("/meal-request/results")
    String showResults(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        final MealRequestSessionState state = sessionState(request.getSession(false));
        if (!(state instanceof SuccessfulMealRequest successfulRequest)) {
            return redirectToInitialRequest();
        }

        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("products", allProducts());
        model.addAttribute("suggestions", successfulRequest.suggestions().suggestions().stream().map(MealSuggestionCard::of).toList());
        return "welcome";
    }

    @GetMapping("/meal-request/recovery")
    String showRecovery(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
        final MealRequestSessionState state = sessionState(request.getSession(false));
        if (!(state instanceof FailedMealRequest failedRequest)) {
            return redirectToInitialRequest();
        }

        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("products", allProducts());
        model.addAttribute("mealRequest", failedRequest.request());
        model.addAttribute("failed", true);
        return "welcome";
    }

    @PostMapping("/meal-request/retry")
    String retryMealRequest(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        final MealRequestSessionState state = sessionState(session);
        if (!(state instanceof FailedMealRequest failedRequest)) {
            return redirectToInitialRequest();
        }

        return storeResultAndRedirect(failedRequest.request(), mealSuggestionService.submit(failedRequest.request()), session);
    }

    @PostMapping("/meal-request/reset")
    String resetMealRequest(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(MEAL_REQUEST_STATE);
        }
        return "redirect:/";
    }

    private static String storeResultAndRedirect(
            final String request,
            final MealRequestResult result,
            final HttpSession session) {
        return switch (result) {
            case MappedMealSuggestions mappedSuggestions -> {
                session.setAttribute(MEAL_REQUEST_STATE, new SuccessfulMealRequest(request, mappedSuggestions));
                yield "redirect:/meal-request/results";
            }
            case FailedRequest(final String failedRequest) -> {
                session.setAttribute(MEAL_REQUEST_STATE, new FailedMealRequest(failedRequest));
                yield "redirect:/meal-request/recovery";
            }
            case InvalidRequest(final String message) -> throw new IllegalStateException("A retained meal request must remain valid");
        };
    }

    private static MealRequestSessionState sessionState(final HttpSession session) {
        if (session == null) {
            return null;
        }
        final Object state = session.getAttribute(MEAL_REQUEST_STATE);
        return state instanceof MealRequestSessionState mealRequestSessionState ? mealRequestSessionState : null;
    }

    private static String redirectToInitialRequest() {
        return "redirect:/?notice=" + NO_ACTIVE_MEAL_REQUEST_NOTICE;
    }

    private List<ProductCard> allProducts() {
        return productCatalogue.allProducts().stream()
                .map(ProductCard::of)
                .toList();
    }
}
