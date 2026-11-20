package demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

final class DemoAccessPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(final CharSequence rawPassword) {
        return bcrypt.encode(digits(rawPassword));
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String digits = digits(rawPassword);
        return !digits.isEmpty() && bcrypt.matches(digits, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(final String encodedPassword) {
        return bcrypt.upgradeEncoding(encodedPassword);
    }

    private static String digits(final CharSequence value) {
        return value.toString().replaceAll("[^0-9]", "");
    }
}
