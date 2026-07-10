package demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import jakarta.servlet.RequestDispatcher;

import module java.base;

@Configuration
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.withObjectPostProcessor(new ObjectPostProcessor<CsrfFilter>() {
                    @Override
                    public CsrfFilter postProcess(final CsrfFilter filter) {
                        filter.setAccessDeniedHandler(errorPageAccessDeniedHandler());
                        return filter;
                    }
                }))
                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(errorPageAccessDeniedHandler()))
                .build();
    }

    private AccessDeniedHandler errorPageAccessDeniedHandler() {
        return (request, response, exception) -> {
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            request.getRequestDispatcher("/error").forward(request, response);
        };
    }
}
