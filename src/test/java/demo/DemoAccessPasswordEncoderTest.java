package demo;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAccessPasswordEncoderTest {

    private final DemoAccessPasswordEncoder encoder = new DemoAccessPasswordEncoder();

    @Test
    void matchesFormattedPhoneNumberAgainstDigitsOnlyHash() {
        final String hash = new BCryptPasswordEncoder().encode("491234567890");

        assertThat(encoder.matches("+49 (123) 456-7890", hash)).isTrue();
        assertThat(encoder.matches("+49 (123) 456-7891", hash)).isFalse();
        assertThat(encoder.matches("+ ()-", hash)).isFalse();
    }

    @Test
    void encodesAndMatchesUsingTheSameNormalization() {
        final String hash = encoder.encode("+49 (123) 456-7890");

        assertThat(encoder.matches("+49 (123) 456-7890", hash)).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("491234567890", hash)).isTrue();
    }

    @Test
    void rejectsInputWithoutAsciiDigitsEvenAgainstAnEmptyHash() {
        final String hash = encoder.encode("+ ()-");

        assertThat(encoder.matches("+ ()-", hash)).isFalse();
    }
}
