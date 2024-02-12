package uk.gov.hmcts.reform.auth.checker.spring.useronly;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;


@Slf4j
public class AuthCheckerUserOnlyFilter<T extends User> extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthorizer<T> userRequestAuthorizer;

    public AuthCheckerUserOnlyFilter(RequestAuthorizer<T> userRequestAuthorizer) {
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

    private T authorizeUser(HttpServletRequest request) {
        try {
            return userRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful user authentication");
            return null;
        }
    }
}
