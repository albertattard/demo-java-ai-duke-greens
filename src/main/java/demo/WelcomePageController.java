package demo;

import module java.base;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class WelcomePageController {

    private final ProductCatalogue productCatalogue;
    private final MealSuggestionService mealSuggestionService;
    private final MealRequestSession mealRequestSession;

    WelcomePageController(
            final ProductCatalogue productCatalogue,
            final MealSuggestionService mealSuggestionService,
            final MealRequestSession mealRequestSession) {
        this.productCatalogue = productCatalogue;
        this.mealSuggestionService = mealSuggestionService;
        this.mealRequestSession = mealRequestSession;
    }

    @GetMapping("/demo")
    String showWelcomePage(@RequestParam(required = false) final String notice, final Model model) {
        model.addAttribute("products", productCatalogue.allProducts().stream().map(ProductCard::of).toList());
        if (mealRequestSession.isBasketUnavailableNotice(notice)) {
            model.addAttribute("informationMessage", mealRequestSession.basketUnavailableMessage());
        } else if (mealRequestSession.isMissingRequestNotice(notice)) {
            model.addAttribute("informationMessage", mealRequestSession.noActiveRequestMessage());
        }
        return "welcome";
    }

    @PostMapping("/demo/meal-request")
    String submitMealRequest(
            @RequestParam(required = false) final String mealRequest,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        final Optional<String> validationError = MealSuggestionService.validationError(mealRequest);
        if (validationError.isPresent()) {
            redirectAttributes.addFlashAttribute("mealRequest", mealRequest);
            redirectAttributes.addFlashAttribute("validationMessage", validationError.get());
            return "redirect:/demo";
        }

        final String previousConversationId = mealRequestSession.conversationId(request);
        if (previousConversationId != null) {
            mealSuggestionService.clearConversation(previousConversationId);
        }
        final String conversationId = mealRequestSession.startConversation(request);
        final MealRequestResult result = mealSuggestionService.submit(conversationId, mealRequest);

        return switch (result) {
            case final SuccessfulMealSuggestions successfulSuggestions -> {
                mealRequestSession.store(request, new SuccessfulMealRequest(mealRequest, successfulSuggestions.assistantMessage(), successfulSuggestions.suggestions()));
                yield mealRequestSession.recommendationsRedirect(conversationId);
            }
            case FailedRequest(final String failedRequest) -> {
                mealRequestSession.store(request, new FailedMealRequest(failedRequest));
                yield mealRequestSession.recommendationsRedirect(conversationId);
            }
            case OutOfScopeRequest(final String outOfScopeRequest) -> {
                mealRequestSession.clear(request);
                redirectAttributes.addFlashAttribute("mealRequest", outOfScopeRequest);
                redirectAttributes.addFlashAttribute("outOfScopeMessage", "Duke Greens helps you find meal ideas. Tell us what you’d like to cook, such as a quick vegetarian dinner for two.");
                yield "redirect:/demo";
            }
            case InvalidRequest(final String message) -> {
                redirectAttributes.addFlashAttribute("mealRequest", mealRequest);
                redirectAttributes.addFlashAttribute("validationMessage", message);
                yield "redirect:/demo";
            }
        };
    }
}
