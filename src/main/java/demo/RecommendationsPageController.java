package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import module java.base;

@Controller
class RecommendationsPageController {

    private final MealSuggestionService mealSuggestionService;
    private final MealRequestSession mealRequestSession;
    private final BasketPresentation basketPresentation;

    RecommendationsPageController(
            final MealSuggestionService mealSuggestionService,
            final MealRequestSession mealRequestSession,
            final BasketPresentation basketPresentation) {
        this.mealSuggestionService = mealSuggestionService;
        this.mealRequestSession = mealRequestSession;
        this.basketPresentation = basketPresentation;
    }

    @GetMapping("/recommendations")
    String showResults(
            @RequestParam(required = false) final Boolean resetConfirmation,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Model model) {
        final MealRequestSessionState state = mealRequestSession.state(request);
        return switch (state) {
            case SuccessfulMealRequest successfulRequest -> {
                response.setHeader("Cache-Control", "no-store");
                addSuccessfulRequest(model, successfulRequest);
                model.addAttribute("resetConfirmationRequired", successfulRequest.needsResetConfirmation() && Boolean.TRUE.equals(resetConfirmation));
                yield "recommendations";
            }
            case FailedMealRequest failedRequest -> {
                response.setHeader("Cache-Control", "no-store");
                model.addAttribute("mealRequest", failedRequest.request());
                model.addAttribute("failed", true);
                yield "recommendations";
            }
            case null ->
                mealRequestSession.initialRequestRedirect();
        };
    }

    @PostMapping("/recommendations/retry")
    String retryMealRequest(final HttpServletRequest request, final RedirectAttributes redirectAttributes) {
        final MealRequestSessionState state = mealRequestSession.state(request);
        if (state instanceof FailedMealRequest failedRequest) {
            return storeResultAndRedirect(
                    failedRequest.request(),
                    mealSuggestionService.submit(failedRequest.request()),
                    request,
                    redirectAttributes);
        }

        return mealRequestSession.initialRequestRedirect();
    }

    @PostMapping("/recommendations/reset")
    String resetMealRequest(
            @RequestParam(required = false) final Boolean confirmed,
            final HttpServletRequest request) {
        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest
                && successfulRequest.needsResetConfirmation()
                && !Boolean.TRUE.equals(confirmed)) {
            return "redirect:/recommendations?resetConfirmation=true";
        }

        mealRequestSession.clear(request);
        return "redirect:/";
    }

    @PostMapping("/recommendations/meals")
    String addMealToBasket(@RequestParam final int index, final HttpServletRequest request) {
        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest) {
            mealRequestSession.store(request, successfulRequest.addMeal(index));
            return "redirect:/recommendations";
        }

        return mealRequestSession.initialRequestRedirect();
    }

    @PostMapping("/recommendations/basket/quantity")
    String changeBasketQuantity(
            @RequestParam final String slug,
            @RequestParam final int quantity,
            final HttpServletRequest request) {
        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest
                && successfulRequest.basket().quantities().containsKey(slug)) {
            mealRequestSession.store(request, successfulRequest.changeBasketQuantity(slug, quantity));
            return "redirect:/recommendations";
        }

        return mealRequestSession.initialRequestRedirect();
    }

    private String storeResultAndRedirect(
            final String mealRequest,
            final MealRequestResult result,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        return switch (result) {
            case MappedMealSuggestions mappedSuggestions -> {
                mealRequestSession.store(request, new SuccessfulMealRequest(mealRequest, mappedSuggestions));
                yield "redirect:/recommendations";
            }
            case FailedRequest(final String failedRequest) -> {
                mealRequestSession.store(request, new FailedMealRequest(failedRequest));
                yield "redirect:/recommendations";
            }
            case OutOfScopeRequest(final String outOfScopeRequest) -> {
                mealRequestSession.clear(request);
                redirectAttributes.addFlashAttribute("mealRequest", outOfScopeRequest);
                redirectAttributes.addFlashAttribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two.");
                yield "redirect:/";
            }
            case InvalidRequest(_) ->
                throw new IllegalStateException("A retained meal request must remain valid");
        };
    }

    private void addSuccessfulRequest(final Model model, final SuccessfulMealRequest request) {
        model.addAttribute("suggestions", IntStream.range(0, request.suggestions().suggestions().size())
                .mapToObj(index -> MealSuggestionCard.of(index, request.suggestions().suggestions().get(index)))
                .toList());
        model.addAttribute("selectedMealIndexes", request.selectedMealIndexes());
        basketPresentation.addTo(model, request);
    }
}
