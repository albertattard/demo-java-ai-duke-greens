package demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JdbcProductCatalogueTest {

    @Autowired
    private ProductCatalogue catalogue;

    @Test
    void makesEverySeededProductAvailable() {
        assertThat(catalogue.allProducts())
                .extracting(Product::slug)
                .containsExactly(
                        "wholewheat-spaghetti-500g",
                        "chickpeas-400g",
                        "brown-rice-500g",
                        "red-lentils-500g",
                        "chopped-tomatoes-400g",
                        "coconut-milk-400ml",
                        "olive-oil-500ml",
                        "onions-1kg",
                        "garlic-100g",
                        "courgettes-500g",
                        "red-peppers-500g",
                        "baby-spinach-200g");
    }

}
