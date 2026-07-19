package demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class CheckoutController {

    private final MealRequestSession mealRequestSession;
    private final BasketPresentation basketPresentation;
    private final MealSuggestionService mealSuggestionService;
    private final FeedbackRepository feedbackRepository;

    CheckoutController(final MealRequestSession mealRequestSession,
                       final BasketPresentation basketPresentation,
                       final MealSuggestionService mealSuggestionService,
                       final FeedbackRepository feedbackRepository) {
        this.mealRequestSession = mealRequestSession;
        this.basketPresentation = basketPresentation;
        this.mealSuggestionService = mealSuggestionService;
        this.feedbackRepository = feedbackRepository;
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

    @PostMapping("/demo/feedback")
    String submitFeedback(final HttpServletRequest request,
                          final HttpServletResponse response,
                          final Model model,
                          @RequestParam(required = false) final String rating,
                          @RequestParam(required = false) final String comment) {
        final FeedbackSubmission feedback;
        try {
            feedback = FeedbackSubmission.from(rating, comment);
        } catch (final IllegalArgumentException exception) {
            response.setHeader("Cache-Control", "no-store");
            model.addAttribute("rating", rating);
            model.addAttribute("comment", comment);
            model.addAttribute("validationMessage", exception.getMessage());
            return "thank-you";
        }

        if (!mealRequestSession.claimFeedbackSubmission(request)) {
            return mealRequestSession.initialRequestRedirect();
        }

        try {
            feedbackRepository.save(feedback);
        } catch (final DataAccessException _) {
            mealRequestSession.restoreFeedbackSubmission(request);
            response.setHeader("Cache-Control", "no-store");
            model.addAttribute("rating", rating);
            model.addAttribute("comment", comment);
            model.addAttribute("validationMessage", "We couldn’t save your feedback. Please try again.");
            return "thank-you";
        }

        mealRequestSession.completeFeedbackSubmission(request);
        return "redirect:/demo/feedback-confirmation";
    }

    @GetMapping("/demo/feedback-confirmation")
    String showFeedbackConfirmation(final HttpServletRequest request, final HttpServletResponse response) {
        if (!mealRequestSession.consumeFeedbackConfirmation(request)) {
            return mealRequestSession.initialRequestRedirect();
        }

        response.setHeader("Cache-Control", "no-store");
        return "feedback-confirmation";
    }
}
