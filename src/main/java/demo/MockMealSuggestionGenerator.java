package demo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import module java.base;

@Component
@Profile("mock")
/// Development-only generator that deliberately returns catalogue-valid meal
/// ideas for every request. It does not emulate the model’s in-scope and
/// out-of-scope classification, so do not use the mock profile to verify that
/// visitor safeguard.
final class MockMealSuggestionGenerator implements MealSuggestionGenerator {

    private static final List<ModelMealSuggestion> RECOMMENDATIONS = List.of(
            new ModelMealSuggestion("Tomato chickpea pasta", 25, "A quick cupboard-friendly pasta dinner.", 1, List.of(
                    new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"),
                    new ModelIngredient("chickpeas-400g", "200", "g"),
                    new ModelIngredient("chopped-tomatoes-400g", "200", "g"))),
            new ModelMealSuggestion("Lentil and spinach curry", 30, "A warm, plant-based dinner with pantry staples.", 1, List.of(
                    new ModelIngredient("red-lentils-500g", "150", "g"),
                    new ModelIngredient("coconut-milk-400ml", "200", "ml"),
                    new ModelIngredient("baby-spinach-200g", "100", "g"))),
            new ModelMealSuggestion("Courgette salmon pasta", 25, "A simple fish dinner with fresh vegetables.", 1, List.of(
                    new ModelIngredient("wholewheat-penne-500g", "200", "g"),
                    new ModelIngredient("salmon-fillets-400g", "200", "g"),
                    new ModelIngredient("courgettes-500g", "200", "g"))),
            new ModelMealSuggestion("Mushroom tofu rice bowl", 30, "A savoury rice bowl for a meat-free evening.", 1, List.of(
                    new ModelIngredient("brown-rice-500g", "150", "g"),
                    new ModelIngredient("mushrooms-400g", "200", "g"),
                    new ModelIngredient("tofu-400g", "200", "g"))));

    @Override
    public ModelMealRequestResponse suggest(final String request, final List<Product> catalogue) {
        final Set<String> catalogueSlugs = catalogue.stream().map(Product::slug).collect(Collectors.toSet());
        final List<ModelMealSuggestion> available = RECOMMENDATIONS.stream()
                .filter(recommendation -> recommendation.ingredients().stream()
                        .map(ModelIngredient::productSlug)
                        .allMatch(catalogueSlugs::contains))
                .collect(Collectors.toCollection(ArrayList::new));

        if (available.isEmpty()) {
            throw new IllegalArgumentException("The catalogue does not support any mock meal recommendations");
        }

        java.util.Collections.shuffle(available);
        final int count = ThreadLocalRandom.current().nextInt(1, Math.min(3, available.size()) + 1);
        return ModelMealRequestResponse.inScope(new ModelMealSuggestions(available.subList(0, count)));
    }
}
