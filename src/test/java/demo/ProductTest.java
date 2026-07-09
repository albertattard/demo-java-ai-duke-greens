package demo;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.Test;

import module java.base;

class ProductTest {

    @Test
    void requiresALowercaseKebabCasePublicSlug() {
        for (final String invalidSlug : List.of("Wholewheat-spaghetti-500g", "wholewheat_spaghetti-500g", " wholewheat-spaghetti-500g ")) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> product(invalidSlug));
        }
    }

    @Test
    void representsThePackageUnitAsADomainMeasurementUnit() {
        assertThat(product("wholewheat-spaghetti-500g").packageUnit())
                .isEqualTo(MeasurementUnit.GRAM);
    }

    @Test
    void requiresANonBlankName() {
        for (final String name : List.of("", "   ")) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> product("wholewheat-spaghetti-500g", name, 500, MeasurementUnit.GRAM, new BigDecimal("1.49")));
        }
        assertThatIllegalArgumentException()
                .isThrownBy(() -> product("wholewheat-spaghetti-500g", null, 500, MeasurementUnit.GRAM, new BigDecimal("1.49")));
    }

    @Test
    void requiresAPositivePackageQuantity() {
        for (final int packageQuantity : List.of(0, -1)) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", packageQuantity, MeasurementUnit.GRAM, new BigDecimal("1.49")));
        }
    }

    @Test
    void requiresAPackageUnit() {
        assertThatNullPointerException()
                .isThrownBy(() -> product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, null, new BigDecimal("1.49")));
    }

    @Test
    void requiresAPositivePriceButAcceptsPricesBelowOneEuro() {
        assertThatCode(() -> product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("0.99")))
                .doesNotThrowAnyException();

        for (final BigDecimal price : List.of(BigDecimal.ZERO, new BigDecimal("-0.01"))) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, price));
        }
        assertThatIllegalArgumentException()
                .isThrownBy(() -> product("wholewheat-spaghetti-500g", "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, null));
    }

    private static Product product(final String slug) {
        return product(slug, "Wholewheat spaghetti", 500, MeasurementUnit.GRAM, new BigDecimal("1.49"));
    }

    private static Product product(
            final String slug,
            final String name,
            final int packageQuantity,
            final MeasurementUnit packageUnit,
            final BigDecimal price) {
        return new Product(slug, name, packageQuantity, packageUnit, price);
    }
}
