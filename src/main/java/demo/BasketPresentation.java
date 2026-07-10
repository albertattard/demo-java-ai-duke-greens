package demo;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import module java.base;

@Component
class BasketPresentation {

    private final ProductCatalogue productCatalogue;

    BasketPresentation(final ProductCatalogue productCatalogue) {
        this.productCatalogue = productCatalogue;
    }

    void addTo(final Model model, final SuccessfulMealRequest request) {
        final List<Product> catalogue = productCatalogue.allProducts();
        model.addAttribute("basketLines", catalogue.stream()
                .filter(product -> request.basket().quantities().containsKey(product.slug()))
                .map(product -> BasketLineCard.of(product, request.basket().quantityOf(product.slug()))).toList());
        model.addAttribute("basketTotal", Strings.formatPrice(request.basket().totalPrice(catalogue)));
        model.addAttribute("basketFulfilsSelectedMeals", request.basket().fulfils(request.selectedMeals()));
    }
}
