package uk.gov.hmcts.auth.checker.spring.useronly;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.auth.checker.RequestAuthorizer;
import uk.gov.hmcts.auth.checker.user.User;
import uk.gov.hmcts.auth.checker.user.UserRequestAuthorizer;
import uk.gov.hmcts.auth.checker.exceptions.AuthCheckerException;


@Slf4j
public class AuthCheckerUserOnlyFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthorizer<User> userRequestAuthorizer;

    public AuthCheckerUserOnlyFilter(RequestAuthorizer<User> userRequestAuthorizer) {
        this.userRequestAuthorizer = userRequestAuthorizer;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return authorizeUser(request);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader(UserRequestAuthorizer.AUTHORISATION);
    }

    private User authorizeUser(HttpServletRequest request) {
        try {
            return userRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful user authentication");
            return null;
        }
    }

}
