package uk.gov.hmcts.reform.auth.checker.spring.serviceanduser;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;


@Slf4j
public class AuthCheckerServiceAndUserFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthorizer<Service> serviceRequestAuthorizer;
    private final RequestAuthorizer<User> userRequestAuthorizer;

    public AuthCheckerServiceAndUserFilter(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                           RequestAuthorizer<User> userRequestAuthorizer) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.userRequestAuthorizer = userRequestAuthorizer;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        Service service = authorizeService(request);
        if (service == null) {
            return null;
        }

        User user = authorizeUser(request);
        if (user == null) {
            return null;
        }

        return new ServiceAndUserPair(service, user);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader(UserRequestAuthorizer.AUTHORISATION);
    }

    private User authorizeUser(HttpServletRequest request) {
        try {
            return userRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful user authentication", e);
            return null;
        }
    }

    private Service authorizeService(HttpServletRequest request) {
        try {
            return serviceRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful service authentication", e);
            return null;
        }
    }

}
