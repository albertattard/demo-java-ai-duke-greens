package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import module java.base;

@Controller
class BasketPageController {

    private final MealRequestSession session;
    private final BasketPresentation presentation;

    BasketPageController(final MealRequestSession session, final BasketPresentation presentation) {
        this.session = session;
        this.presentation = presentation;
    }

    @GetMapping("/basket/{conversationId}")
    String show(
            @PathVariable final String conversationId,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Model model) {
        if (!session.hasConversation(request, conversationId)
                || !(session.state(request) instanceof SuccessfulMealRequest successful)) {
            return session.unavailableBasketRedirect(request);
        }

        response.setHeader("Cache-Control", "no-store");
        presentation.addTo(model, successful);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("mealChoices", successful.resultSets().stream()
                .flatMap(set -> IntStream.range(0, set.suggestions().suggestions().size())
                .filter(index -> !set.dismissedMealIndexes().contains(index))
                .mapToObj(index -> Map.of(
                    "key", SuccessfulMealRequest.key(successful.resultSets().indexOf(set), index),
                    "name", set.suggestions().suggestions().get(index).name(),
                    "selected", successful.selected(successful.resultSets().indexOf(set), index))))
                .toList());
        return "basket";
    }

    @PostMapping("/basket/{conversationId}")
    String update(
            @PathVariable final String conversationId,
            @RequestParam(required = false, name = "meal") final java.util.Set<String> meals,
            final HttpServletRequest request) {
        if (!session.hasConversation(request, conversationId)
                || !(session.state(request) instanceof SuccessfulMealRequest successful)) {
            return session.unavailableBasketRedirect(request);
        }

        session.store(request, successful.selectMeals(meals == null ? java.util.Set.of() : meals));
        return "redirect:/basket/" + conversationId;
    }
}
