package uk.gov.hmcts.reform.auth.checker.spring;


import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import uk.gov.hmcts.reform.auth.checker.core.CachingSubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.UserResolver;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenDetails;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;

@Lazy
@Configuration
public class AuthCheckerConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serviceResolver")
    public SubjectResolver<Service> serviceResolver(ServiceTokenParser serviceTokenParser, AuthCheckerProperties properties) {
        return new CachingSubjectResolver<>(new ServiceResolver(serviceTokenParser), properties.getService().getTtlInSeconds(), properties.getService().getMaximumSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "userResolver")
    public SubjectResolver<User> userResolver(UserTokenParser<UserTokenDetails> userTokenParser, AuthCheckerProperties properties) {
        return new CachingSubjectResolver<>(new UserResolver(userTokenParser), properties.getUser().getTtlInSeconds(), properties.getUser().getMaximumSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceRequestAuthorizer")
    public ServiceRequestAuthorizer serviceRequestAuthorizer(SubjectResolver<Service> serviceResolver,
                                                             @Qualifier("authorizedServiceExtractor") Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor) {
        return new ServiceRequestAuthorizer(serviceResolver, authorizedServicesExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = "userRequestAuthorizer")
    public UserRequestAuthorizer userRequestAuthorizer(SubjectResolver<User> userResolver,
                                                       Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                                       @Qualifier("authorizedRolesExtractor") Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor) {
        return new UserRequestAuthorizer(userResolver, userIdExtractor, authorizedRolesExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(name = "preAuthenticatedAuthenticationProvider")
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new AuthCheckerUserDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(preAuthenticatedAuthenticationProvider));
    }
}
