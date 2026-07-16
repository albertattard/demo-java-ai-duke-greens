package demo;

import module java.base;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/// Development-only generator that deliberately returns catalogue-valid meal
/// ideas for every request. It does not emulate the model’s in-scope and
/// out-of-scope classification, so do not use the mock profile to verify that
/// visitor safeguard.
@Component
@Profile("mock")
final class MockMealSuggestionGenerator implements MealSuggestionGenerator {

    static final List<ModelMealSuggestion> RECOMMENDATIONS = List.of(
            new ModelMealSuggestion("Tomato chickpea pasta", 25, "A quick cupboard-friendly pasta dinner.", 1, List.of(
                    new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"),
                    new ModelIngredient("chickpeas-400g", "200", "g"),
                    new ModelIngredient("chopped-tomatoes-400g", "200", "g"))),
            new ModelMealSuggestion("Lentil and spinach curry", 30, "A warm, plant-based dinner with pantry staples.", 1, List.of(
                    new ModelIngredient("red-lentils-500g", "150", "g"),
                    new ModelIngredient("coconut-milk-400ml", "200", "ml"),
                    new ModelIngredient("baby-spinach-200g", "100", "g"))),
            new ModelMealSuggestion("Courgette tofu pasta", 25, "A simple plant-based pasta with fresh vegetables.", 1, List.of(
                    new ModelIngredient("wholewheat-penne-500g", "200", "g"),
                    new ModelIngredient("tofu-400g", "200", "g"),
                    new ModelIngredient("courgettes-500g", "200", "g"))),
            new ModelMealSuggestion("Mushroom tofu rice bowl", 30, "A savoury rice bowl for a meat-free evening.", 1, List.of(
                    new ModelIngredient("brown-rice-500g", "150", "g"),
                    new ModelIngredient("mushrooms-400g", "200", "g"),
                    new ModelIngredient("tofu-400g", "200", "g"))),
            new ModelMealSuggestion("Pepper and bean couscous", 20, "A colourful, quick dinner with cupboard staples.", 1, List.of(
                    new ModelIngredient("couscous-500g", "150", "g"),
                    new ModelIngredient("kidney-beans-400g", "200", "g"),
                    new ModelIngredient("red-peppers-500g", "150", "g"))),
            new ModelMealSuggestion("Broccoli quinoa bowl", 30, "A hearty vegetable bowl with a bright dressing.", 1, List.of(
                    new ModelIngredient("quinoa-500g", "150", "g"),
                    new ModelIngredient("broccoli-500g", "200", "g"),
                    new ModelIngredient("lemons-300g", "50", "g"))),
            new ModelMealSuggestion("Tomato and spinach penne", 25, "An easy pasta dinner with a rich tomato sauce.", 1, List.of(
                    new ModelIngredient("wholewheat-penne-500g", "200", "g"),
                    new ModelIngredient("passata-700ml", "200", "ml"),
                    new ModelIngredient("baby-spinach-200g", "100", "g"))),
            new ModelMealSuggestion("Sweet potato chickpea curry", 35, "A comforting curry with coconut milk and greens.", 1, List.of(
                    new ModelIngredient("sweet-potatoes-1kg", "300", "g"),
                    new ModelIngredient("chickpeas-400g", "200", "g"),
                    new ModelIngredient("coconut-milk-400ml", "200", "ml"))),
            new ModelMealSuggestion("Mushroom lentil spaghetti", 30, "A satisfying lentil and mushroom pasta dinner.", 1, List.of(
                    new ModelIngredient("wholewheat-spaghetti-500g", "200", "g"),
                    new ModelIngredient("red-lentils-500g", "150", "g"),
                    new ModelIngredient("mushrooms-400g", "200", "g"))));

    @Override
    public ModelMealRequestResponse suggest(final Request request) {
        final Set<String> catalogueSlugs = request.catalogue().stream().map(Product::slug).collect(Collectors.toSet());
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
        return ModelMealRequestResponse.inScope("Here are some meal ideas from Duke Greens.", new ModelMealSuggestions(available.subList(0, count)));
    }
}
