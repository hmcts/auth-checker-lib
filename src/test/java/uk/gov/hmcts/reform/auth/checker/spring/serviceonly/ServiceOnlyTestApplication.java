package uk.gov.hmcts.reform.auth.checker.spring.serviceonly;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.ServiceResolverBackdoor;

@SpringBootApplication
public class ServiceOnlyTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOnlyTestApplication.class, args);
    }

    @RestController
    public static class TestController {
        @RequestMapping("/test")
        public String publicEndpoint() {
            ServiceDetails details = (ServiceDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return details.getUsername();
        }
    }

    @Configuration
    @EnableWebSecurity
    public class AuthCheckerConfiguration {
        @Bean
        @Qualifier("authorizedServiceExtractor")
        public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
            return (any) -> Collections.singletonList("divorce");
        }

        @Bean
        public SubjectResolver<Service> serviceResolver() {
            return new ServiceResolverBackdoor();
        }

        @Bean
        public AuthCheckerServiceOnlyFilter authCheckerServiceOnlyFilter(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                                                         AuthenticationManager authenticationManager) {
            AuthCheckerServiceOnlyFilter filter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);
            filter.setAuthenticationManager(authenticationManager);
            return filter;
        }
    }

    @Configuration
    @EnableWebSecurity
    public class SecurityConfiguration {

        @Autowired
        private AuthCheckerServiceOnlyFilter filter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .addFilter(filter)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                    authorizeHttpRequests.anyRequest().authenticated()
                );
            return http.build();
        }
    }
}
