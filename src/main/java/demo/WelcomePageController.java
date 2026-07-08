package demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import module java.base;

@Controller
class WelcomePageController {

    private final ProductCatalogue productCatalogue;

    WelcomePageController(final ProductCatalogue productCatalogue) {
        this.productCatalogue = productCatalogue;
    }

    @GetMapping("/")
    String showWelcomePage(final Model model) {
        model.addAttribute("products", productCatalogue.allProducts().stream()
                .map(product -> new ProductCard(
                        product.name(),
                        product.packageQuantity() + " " + product.packageUnit(),
                        formatPrice(product.price(), product.currency())))
                .toList());
        return "welcome";
    }

    private static String formatPrice(final BigDecimal price, final Currency currency) {
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        formatter.setCurrency(currency);
        return formatter.format(price);
    }

    record ProductCard(String name, String packageDetail, String formattedPrice) {}
}
