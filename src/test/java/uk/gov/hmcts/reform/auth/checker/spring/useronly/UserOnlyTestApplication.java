package uk.gov.hmcts.reform.auth.checker.spring.useronly;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.UserResolverBackdoor;

@SpringBootApplication
public class UserOnlyTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserOnlyTestApplication.class, args);
    }

    @RestController
    public static class TestController {
        @RequestMapping("/test")
        public String publicEndpoint() {
            UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return details.getUsername();
        }
    }

    @Configuration
    @EnableWebSecurity
    public class AuthCheckerConfiguration {
        @Bean
        public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
            return (request) -> Optional.of("1");
        }

        @Bean
        public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
            return (any) -> Collections.singletonList("citizen");
        }

        @Bean
        public SubjectResolver<User> userResolver() {
            return new UserResolverBackdoor();
        }

        @Bean
        public AuthCheckerUserOnlyFilter authCheckerUserOnlyFilter(RequestAuthorizer<User> userAuthClient,
                                                                   AuthenticationManager authenticationManager) {
            AuthCheckerUserOnlyFilter filter = new AuthCheckerUserOnlyFilter(userAuthClient);
            filter.setAuthenticationManager(authenticationManager);
            return filter;
        }
    }

    @Configuration
    @EnableWebSecurity
    public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthCheckerUserOnlyFilter filter;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .addFilter(filter)
                .authorizeRequests().anyRequest().authenticated();
        }
    }
}
