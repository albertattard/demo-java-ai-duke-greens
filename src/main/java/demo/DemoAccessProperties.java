package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("duke-greens.demo-access")
record DemoAccessProperties(String accessCodeHash) {

    DemoAccessProperties {
        if (accessCodeHash == null || !accessCodeHash.startsWith("$2")) {
            throw new IllegalStateException("duke-greens.demo-access.access-code-hash must be a BCrypt hash supplied through deployment configuration");
        }
    }
}
