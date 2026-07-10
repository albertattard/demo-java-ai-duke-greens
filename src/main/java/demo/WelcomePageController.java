package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import module java.base;

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
            final Model model) {
        final MealRequestResult result = mealSuggestionService.submit(mealRequest);
        if (result instanceof InvalidRequest(final String message)) {
            addProducts(model);
            model.addAttribute("validationMessage", message);
            return "welcome";
        }

        return switch (result) {
            case MappedMealSuggestions mappedSuggestions -> {
                mealRequestSession.store(request, new SuccessfulMealRequest(mealRequest, mappedSuggestions));
                yield "redirect:/recommendations";
            }
            case FailedRequest(final String failedRequest) -> {
                mealRequestSession.store(request, new FailedMealRequest(failedRequest));
                yield "redirect:/recommendations";
            }
            case InvalidRequest(_) -> throw new IllegalStateException("A retained meal request must remain valid");
        };
    }

    private void addProducts(final Model model) {
        model.addAttribute("products", productCatalogue.allProducts().stream().map(ProductCard::of).toList());
    }
}
