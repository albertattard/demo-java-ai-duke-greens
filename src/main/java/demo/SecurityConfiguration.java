package demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.RequestDispatcher;

import module java.base;

@Configuration
class SecurityConfiguration {

    private static final String DEMO_ACCESS_USERNAME = "demo-visitor";
    private final DemoAccessProperties demoAccessProperties;

    SecurityConfiguration(final DemoAccessProperties demoAccessProperties) {
        this.demoAccessProperties = demoAccessProperties;
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        final LoginUrlAuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/demo-access");
        return http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/demo", "/demo/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/demo-access")
                        .loginProcessingUrl("/demo-access")
                        .usernameParameter("accessCode")
                        .passwordParameter("accessCode")
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/demo/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .csrf(csrf -> csrf.withObjectPostProcessor(new ObjectPostProcessor<CsrfFilter>() {
                    @Override
                    public CsrfFilter postProcess(final CsrfFilter filter) {
                        filter.setAccessDeniedHandler(errorPageAccessDeniedHandler());
                        return filter;
                    }
                }))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor((request, response, exception) -> {
                            if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setHeader("HX-Redirect", "/demo-access");
                                return;
                            }
                            loginEntryPoint.commence(request, response, exception);
                        }, request -> request.getRequestURI().startsWith("/demo"))
                        .accessDeniedHandler(errorPageAccessDeniedHandler()))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> User.withUsername(DEMO_ACCESS_USERNAME)
                .password(demoAccessProperties.accessCodeHash())
                .authorities("DEMO_VISITOR")
                .build();
    }

    private DaoAuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> response.sendRedirect(UriComponentsBuilder
                .fromPath("/demo-access")
                .queryParam("error")
                .build()
                .toUriString());
    }

    private AccessDeniedHandler errorPageAccessDeniedHandler() {
        return (request, response, exception) -> {
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            request.getRequestDispatcher("/error").forward(request, response);
        };
    }
}
