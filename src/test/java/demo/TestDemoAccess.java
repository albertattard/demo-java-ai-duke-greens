package demo;

import java.util.UUID;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.support.TestPropertySourceUtils;

public final class TestDemoAccess implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String ACCESS_CODE = UUID.randomUUID().toString();

    public static String accessCode() {
        return ACCESS_CODE;
    }

    @Override
    public void initialize(final ConfigurableApplicationContext context) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "duke-greens.demo-access.access-code-hash=" + new BCryptPasswordEncoder().encode(ACCESS_CODE));
    }
}
