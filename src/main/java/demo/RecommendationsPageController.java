package demo;

import module java.base;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/demo/recommendations/{conversationId}")
    String showConversationResults(
            @PathVariable final String conversationId,
            @RequestParam(required = false) final Boolean resetConfirmation,
            @RequestParam(required = false) final String notice,
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
                model.addAttribute("basketUnavailable", mealRequestSession.isBasketUnavailableNotice(notice));
                yield "recommendations";
            }
            case FailedMealRequest failedRequest -> {
                response.setHeader("Cache-Control", "no-store");
                model.addAttribute("mealRequest", failedRequest.request());
                model.addAttribute("failed", true);
                model.addAttribute("resetConfirmationRequired", false);
                model.addAttribute("conversationId", conversationId);
                yield "recommendations";
            }
            case FailedRefinementRequest _ -> mealRequestSession.initialRequestRedirect();
            case null -> {
                model.addAttribute("resetConfirmationRequired", false);
                yield mealRequestSession.initialRequestRedirect();
            }
        };
    }

    @PostMapping("/demo/recommendations/{conversationId}/retry")
    String retryMealRequest(
            @PathVariable final String conversationId,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        return switch (mealRequestSession.state(request)) {
            case final FailedMealRequest failedRequest -> storeResultAndRedirect(
                    failedRequest.request(),
                    mealSuggestionService.submit(new MealSuggestionService.Request(conversationId, failedRequest.request())),
                    request);
            case null, default -> mealRequestSession.initialRequestRedirect();
        };
    }

    @PostMapping("/demo/recommendations/{conversationId}/reset")
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
        return "redirect:/demo";
    }

    @PostMapping("/demo/recommendations/{conversationId}/meals")
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

    @PostMapping("/demo/recommendations/{conversationId}/meals/remove")
    String removeMealFromBasket(
            @PathVariable final String conversationId,
            @RequestParam final int index,
            @RequestParam final int resultSet,
            final HttpServletRequest request) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        if (mealRequestSession.state(request) instanceof final SuccessfulMealRequest successfulRequest) {
            mealRequestSession.store(request, successfulRequest.removeMeal(resultSet, index));
            return mealRequestSession.recommendationsRedirect(request);
        }

        return mealRequestSession.initialRequestRedirect();
    }

    @PostMapping("/demo/recommendations/{conversationId}/follow-up")
    String followUp(
            @PathVariable final String conversationId,
            @RequestParam final String followUp,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {

        if (!mealRequestSession.hasConversation(request, conversationId)) {
            return mealRequestSession.initialRequestRedirect();
        }

        final MealRequestSessionState state = mealRequestSession.state(request);
        if (!(state instanceof final SuccessfulMealRequest successfulRequest)) {
            return mealRequestSession.recommendationsRedirect(request);
        }

        final MealRequestResult validation = mealSuggestionService.submit(new MealSuggestionService.Request(
                conversationId,
                followUp,
                successfulRequest.latestRecommendationNames(),
                successfulRequest.selectedMealNames()));

        if (validation instanceof InvalidRequest(String message)) {
            redirectAttributes.addFlashAttribute("followUpError", message);
            return mealRequestSession.recommendationsRedirect(request);
        }

        if (validation instanceof SuccessfulMealSuggestions(final String assistantMessage, final List<MappedMealSuggestion> suggestions)) {
            mealRequestSession.store(request, successfulRequest.appendFollowUp(followUp, assistantMessage, suggestions));
        } else if (validation instanceof FailedRequest(final String errorMessage)) {
            redirectAttributes.addFlashAttribute("failedFollowUp", errorMessage);
        }
        return mealRequestSession.recommendationsRedirect(request);
    }

    private String storeResultAndRedirect(
            final String mealRequest,
            final MealRequestResult result,
            final HttpServletRequest request) {

        return switch (result) {
            case SuccessfulMealSuggestions successfulSuggestions -> {
                final String conversationId = mealRequestSession.conversationId(request);
                if (conversationId == null) {
                    throw new IllegalStateException("Conversation ID not found!");
                }
                mealRequestSession.store(request, new SuccessfulMealRequest(mealRequest, successfulSuggestions.assistantMessage(), successfulSuggestions.suggestions()));
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
            case InvalidRequest(_) -> throw new IllegalStateException("A retained meal request must remain valid");
        };
    }

    private void addSuccessfulRequest(final Model model, final SuccessfulMealRequest request) {
        final int currentSet = request.resultSets().size() - 1;
        model.addAttribute("recommendations", Stream.concat(
                        IntStream.range(0, request.resultSets().getLast().suggestions().size())
                                .filter(index -> !request.selected(currentSet, index))
                                .mapToObj(index -> MealSuggestionCard.of(currentSet, index, request.resultSets().getLast().suggestions().get(index))),
                        IntStream.range(0, currentSet)
                                .boxed()
                                .flatMap(resultSet -> IntStream.range(0, request.resultSets().get(resultSet).suggestions().size())
                                        .filter(index -> request.returned(resultSet, index))
                                        .mapToObj(index -> MealSuggestionCard.of(resultSet, index, request.resultSets().get(resultSet).suggestions().get(index)))))
                .toList());
        model.addAttribute("basketMeals", IntStream.range(0, request.resultSets().size())
                .boxed()
                .flatMap(resultSet -> IntStream.range(0, request.resultSets().get(resultSet).suggestions().size())
                        .filter(index -> request.selected(resultSet, index))
                        .mapToObj(index -> MealSuggestionCard.of(resultSet, index, request.resultSets().get(resultSet).suggestions().get(index))))
                .toList());
        model.addAttribute("currentResultSet", currentSet);
        model.addAttribute("transcript", request.transcript());
        model.addAttribute("selectedMealKeys", request.selectedMealKeys());
        basketPresentation.addTo(model, request);
    }
}
