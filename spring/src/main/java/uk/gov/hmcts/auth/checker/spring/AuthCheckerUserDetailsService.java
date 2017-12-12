package uk.gov.hmcts.auth.checker.spring;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.gov.hmcts.auth.checker.service.Service;
import uk.gov.hmcts.auth.checker.user.User;
import uk.gov.hmcts.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.auth.checker.spring.serviceanduser.ServiceAndUserPair;
import uk.gov.hmcts.auth.checker.spring.serviceonly.ServiceDetails;
import uk.gov.hmcts.auth.checker.spring.useronly.UserDetails;

public class AuthCheckerUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        Object principal = token.getPrincipal();

        if (principal instanceof Service) {
            return new ServiceDetails(((Service) principal).getPrincipal());
        }

        if (principal instanceof User) {
            User user = (User) principal;
            return new UserDetails(user.getPrincipal(), (String) token.getCredentials(), user.getRoles());
        }

        ServiceAndUserPair serviceAndUserPair = (ServiceAndUserPair) principal;
        return new ServiceAndUserDetails(
            serviceAndUserPair.getUser().getPrincipal(),
            (String) token.getCredentials(),
            serviceAndUserPair.getUser().getRoles(),
            serviceAndUserPair.getService().getPrincipal()
        );
    }
}
