package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/recommendations/{conversationId}")
    String showConversationResults(
            @PathVariable final String conversationId,
            @RequestParam(required = false) final Boolean resetConfirmation,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Model model) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);
        return switch (state) {
            case SuccessfulMealRequest successfulRequest -> {
                response.setHeader("Cache-Control", "no-store");
                addSuccessfulRequest(model, successfulRequest);
                model.addAttribute("conversationId", conversationId);
                model.addAttribute("resetConfirmationRequired", successfulRequest.needsResetConfirmation() && Boolean.TRUE.equals(resetConfirmation));
                yield "recommendations";
            }
            case FailedMealRequest failedRequest -> {
                response.setHeader("Cache-Control", "no-store");
                model.addAttribute("mealRequest", failedRequest.request());
                model.addAttribute("failed", true);
                model.addAttribute("conversationId", conversationId);
                yield "recommendations";
            }
            case FailedRefinementRequest failedRefinement -> {
                response.setHeader("Cache-Control", "no-store");
                addSuccessfulRequest(model, failedRefinement.request());
                model.addAttribute("refinementFailed", true);
                model.addAttribute("conversationId", conversationId);
                yield "recommendations";
            }
            case null ->
                mealRequestSession.initialRequestRedirect();
        };
    }

    @PostMapping("/recommendations/{conversationId}/retry")
    String retryMealRequest(
            @PathVariable final String conversationId,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        return switch (mealRequestSession.state(request)) {
            case final FailedMealRequest failedRequest ->
                storeResultAndRedirect(
                failedRequest.request(),
                mealSuggestionService.submit(conversationId, failedRequest.request()),
                request,
                redirectAttributes);
            case final FailedRefinementRequest failedRefinement -> {
                final MealRequestResult result = mealSuggestionService.refine(
                        conversationId,
                        failedRefinement.request().pendingRefinement(),
                        failedRefinement.request().selectedMealNames(),
                        failedRefinement.request().dismissedMealNames());
                yield storeRefinementResult(failedRefinement.request(), result, request);
            }
            case null, default ->
                mealRequestSession.initialRequestRedirect();
        };
    }

    @PostMapping("/recommendations/{conversationId}/reset")
    String resetMealRequest(
            @PathVariable final String conversationId,
            @RequestParam(required = false) final Boolean confirmed,
            final HttpServletRequest request) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest
                && successfulRequest.needsResetConfirmation()
                && !Boolean.TRUE.equals(confirmed)) {
            return mealRequestSession.recommendationsRedirect(conversationId) + "?resetConfirmation=true";
        }

        mealSuggestionService.clearConversation(conversationId);
        mealRequestSession.clear(request);
        return "redirect:/";
    }

    @PostMapping("/recommendations/{conversationId}/meals")
    String addMealToBasket(
            @PathVariable final String conversationId,
            @RequestParam final int index,
            @RequestParam(defaultValue = "0") final int resultSet,
            final HttpServletRequest request) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest) {
            mealRequestSession.store(request, successfulRequest.addMeal(resultSet, index));
            return mealRequestSession.recommendationsRedirect(request);
        }

        return mealRequestSession.initialRequestRedirect();
    }

    @PostMapping("/recommendations/{conversationId}/meals/dismissal")
    String toggleDismissal(
            @PathVariable final String conversationId,
            @RequestParam final int index,
            final HttpServletRequest request) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof SuccessfulMealRequest successfulRequest) {
            mealRequestSession.store(request, successfulRequest.toggleDismissal(index));
            return mealRequestSession.recommendationsRedirect(request);
        }

        return mealRequestSession.initialRequestRedirect();
    }

    @PostMapping("/recommendations/{conversationId}/refine")
    String refine(
            @PathVariable final String conversationId,
            @RequestParam final String refinement,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);
        if (!(state instanceof SuccessfulMealRequest successfulRequest) || !successfulRequest.canRefine()) {
            return mealRequestSession.recommendationsRedirect(request);
        }

        final MealRequestResult validation = mealSuggestionService.refine(
                conversationId,
                refinement,
                successfulRequest.selectedMealNames(),
                successfulRequest.dismissedMealNames());

        if (validation instanceof InvalidRequest invalid) {
            redirectAttributes.addFlashAttribute("refinementError", invalid.message());
            return mealRequestSession.recommendationsRedirect(request);
        }

        return storeRefinementResult(successfulRequest.prepareRefinement(refinement), validation, request);
    }

    private String storeRefinementResult(final SuccessfulMealRequest successfulRequest, final MealRequestResult result, final HttpServletRequest request) {
        if (result instanceof MappedMealSuggestions suggestions) {
            mealRequestSession.store(request, successfulRequest.appendRefinement(suggestions));
        } else {
            mealRequestSession.store(request, new FailedRefinementRequest(successfulRequest));
        }
        return mealRequestSession.recommendationsRedirect(request);
    }

    @PostMapping("/recommendations/{conversationId}/basket/quantity")
    String changeBasketQuantity(
            @PathVariable final String conversationId,
            @RequestParam final String slug,
            @RequestParam final int quantity,
            final HttpServletRequest request) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);

        if (state instanceof final SuccessfulMealRequest successfulRequest
                && successfulRequest.basket().quantities().containsKey(slug)) {
            mealRequestSession.store(request, successfulRequest.changeBasketQuantity(slug, quantity));
            return mealRequestSession.recommendationsRedirect(request);
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
                final String conversationId = mealRequestSession.conversationId(request);
                if (conversationId == null) {
                    throw new IllegalStateException("Conversation ID not found!");
                }
                mealRequestSession.store(request, new SuccessfulMealRequest(mealRequest, mappedSuggestions));
                yield mealRequestSession.recommendationsRedirect(conversationId);
            }
            case FailedRequest(final String failedRequest) -> {
                final String conversationId = mealRequestSession.conversationId(request);
                if (conversationId == null) {
                    throw new IllegalStateException("Conversation ID not found!");
                }
                mealRequestSession.store(request, new FailedMealRequest(failedRequest));
                yield mealRequestSession.recommendationsRedirect(conversationId);
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
        model.addAttribute("resultSets", IntStream.range(0, request.resultSets().size()).mapToObj(set -> Map.of(
                "index", set,
                "suggestions", IntStream.range(0, request.resultSets().get(set).suggestions().suggestions().size()).mapToObj(index -> MealSuggestionCard.of(index, request.resultSets().get(set).suggestions().suggestions().get(index))).toList(),
                "dismissed", request.resultSets().get(set).dismissedMealIndexes(),
                "locked", request.resultSets().get(set).feedbackLocked())).toList());
        // Compatibility for existing MVC coverage; visitor rendering uses resultSets.
        model.addAttribute("suggestions", request.resultSets().getFirst().suggestions().suggestions().stream().map(suggestion -> MealSuggestionCard.of(request.resultSets().getFirst().suggestions().suggestions().indexOf(suggestion), suggestion)).toList());
        model.addAttribute("selectedMealKeys", request.selectedMealKeys());
        model.addAttribute("canRefine", request.canRefine());
        basketPresentation.addTo(model, request);
    }
}
