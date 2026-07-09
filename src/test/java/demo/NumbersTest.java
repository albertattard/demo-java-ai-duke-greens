package demo;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import module java.base;

class NumbersTest {

    @Test
    void acceptsFractionalValuesGreaterThanZero() {
        assertThatCode(() -> Numbers.requireGreaterThanZero(new BigDecimal("0.99"), "must be positive"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-0.01"})
    void rejectsValuesThatAreNotGreaterThanZero(final BigDecimal value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Numbers.requireGreaterThanZero(value, "must be positive"));
    }
}
