package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

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

    @GetMapping("/")
    String showWelcomePage(@RequestParam(required = false) final String notice, final Model model) {
        addProducts(model);
        if (mealRequestSession.isMissingRequestNotice(notice)) {
            model.addAttribute("informationMessage", mealRequestSession.noActiveRequestMessage());
        }
        return "welcome";
    }

    @PostMapping("/meal-request")
    String submitMealRequest(
            @RequestParam(required = false) final String mealRequest,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        final MealRequestResult result = mealSuggestionService.submit(mealRequest);

        return switch (result) {
            case final MappedMealSuggestions mappedSuggestions -> {
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
            case InvalidRequest(final String message) -> {
                redirectAttributes.addFlashAttribute("mealRequest", mealRequest);
                redirectAttributes.addFlashAttribute("validationMessage", message);
                yield "redirect:/";
            }
        };
    }

    private void addProducts(final Model model) {
        model.addAttribute("products", productCatalogue.allProducts().stream().map(ProductCard::of).toList());
    }
}
