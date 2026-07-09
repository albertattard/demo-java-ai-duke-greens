package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import module java.base;

@Controller
class WelcomePageController {

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
            final Model model) {
        final MealRequestResult result = mealSuggestionService.submit(mealRequest);
        model.addAttribute("products", allProducts());

        switch (result) {
            case MappedMealSuggestions(final List<MappedMealSuggestion> suggestions) ->
                model.addAttribute("suggestions", suggestions.stream().map(MealSuggestionCard::of).toList());
            case InvalidRequest(final String message) ->
                model.addAttribute("validationMessage", message);
            case FailedRequest(final String request) -> {
                model.addAttribute("mealRequest", request);
                model.addAttribute("failed", true);
            }
        }

        return "welcome";
    }

    private List<ProductCard> allProducts() {
        return productCatalogue.allProducts().stream()
                .map(ProductCard::of)
                .toList();
    }
}
