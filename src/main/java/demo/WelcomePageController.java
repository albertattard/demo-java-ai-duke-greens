package demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import module java.base;

@Controller
class WelcomePageController {

    private static final String MEAL_REQUEST_STATE = "mealRequestState";
    private static final String NO_ACTIVE_MEAL_REQUEST = "There is no active meal request to display.";

    private final ProductCatalogue productCatalogue;
    private final MealSuggestionService mealSuggestionService;

    WelcomePageController(
            final ProductCatalogue productCatalogue,
            final MealSuggestionService mealSuggestionService) {
        this.productCatalogue = productCatalogue;
        this.mealSuggestionService = mealSuggestionService;
    }

    @GetMapping("/")
    String showWelcomePage(final Model model) {
        model.addAttribute("products", allProducts());
        return "welcome";
    }

    @PostMapping("/meal-request")
    String submitMealRequest(
            @RequestParam(required = false) final String mealRequest,
            final HttpSession session,
            final Model model) {
        final MealRequestResult result = mealSuggestionService.submit(mealRequest);

        return switch (result) {
            case MappedMealSuggestions mappedSuggestions -> {
                session.setAttribute(MEAL_REQUEST_STATE, new SuccessfulMealRequest(mealRequest, mappedSuggestions));
                yield "redirect:/meal-request/results";
            }
            case InvalidRequest(final String message) -> {
                model.addAttribute("products", allProducts());
                model.addAttribute("validationMessage", message);
                yield "welcome";
            }
            case FailedRequest(final String request) -> {
                session.setAttribute(MEAL_REQUEST_STATE, new FailedMealRequest(request));
                yield "redirect:/meal-request/recovery";
            }
        };
    }

    @GetMapping("/meal-request/results")
    String showResults(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        final MealRequestSessionState state = sessionState(session);
        if (!(state instanceof SuccessfulMealRequest successfulRequest)) {
            return redirectToInitialRequest(redirectAttributes);
        }

        model.addAttribute("products", allProducts());
        model.addAttribute("suggestions", successfulRequest.suggestions().suggestions().stream().map(MealSuggestionCard::of).toList());
        return "welcome";
    }

    @GetMapping("/meal-request/recovery")
    String showRecovery(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        final MealRequestSessionState state = sessionState(session);
        if (!(state instanceof FailedMealRequest failedRequest)) {
            return redirectToInitialRequest(redirectAttributes);
        }

        model.addAttribute("products", allProducts());
        model.addAttribute("mealRequest", failedRequest.request());
        model.addAttribute("failed", true);
        return "welcome";
    }

    @PostMapping("/meal-request/retry")
    String retryMealRequest(final HttpSession session, final RedirectAttributes redirectAttributes) {
        final MealRequestSessionState state = sessionState(session);
        if (!(state instanceof FailedMealRequest failedRequest)) {
            return redirectToInitialRequest(redirectAttributes);
        }

        return storeResultAndRedirect(failedRequest.request(), session);
    }

    @PostMapping("/meal-request/reset")
    String resetMealRequest(final HttpSession session) {
        session.removeAttribute(MEAL_REQUEST_STATE);
        return "redirect:/";
    }

    private String storeResultAndRedirect(final String request, final HttpSession session) {
        return switch (mealSuggestionService.submit(request)) {
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
        final Object state = session.getAttribute(MEAL_REQUEST_STATE);
        return state instanceof MealRequestSessionState mealRequestSessionState ? mealRequestSessionState : null;
    }

    private static String redirectToInitialRequest(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("informationMessage", NO_ACTIVE_MEAL_REQUEST);
        return "redirect:/";
    }

    private List<ProductCard> allProducts() {
        return productCatalogue.allProducts().stream()
                .map(ProductCard::of)
                .toList();
    }
}
