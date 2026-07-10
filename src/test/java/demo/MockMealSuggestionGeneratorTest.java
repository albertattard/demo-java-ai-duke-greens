package demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import module java.base;

@SpringBootTest
@ActiveProfiles("mock")
class MockMealSuggestionGeneratorTest {

    @Autowired
    private MealSuggestionGenerator generator;

    @Autowired
    private ProductCatalogue productCatalogue;

    @Test
    void suppliesCatalogueValidRecommendationsForArbitraryRequestsWithoutOpenAi() {
        final List<Product> catalogue = productCatalogue.allProducts();

        final ModelMealSuggestions suggestions = generator.suggest("This request should not affect the mock response.", catalogue);

        assertThat(suggestions.suggestions()).isNotEmpty();
        assertThat(suggestions.suggestions())
                .flatMap(ModelMealSuggestion::ingredients)
                .extracting(ModelIngredient::productSlug)
                .allMatch(slug -> catalogue.stream().map(Product::slug).anyMatch(slug::equals));
    }
}
