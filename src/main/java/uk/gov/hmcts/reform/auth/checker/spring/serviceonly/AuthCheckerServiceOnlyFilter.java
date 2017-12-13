package uk.gov.hmcts.reform.auth.checker.spring.serviceonly;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;

@Slf4j
public class AuthCheckerServiceOnlyFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthorizer<Service> serviceRequestAuthorizer;

    public AuthCheckerServiceOnlyFilter(RequestAuthorizer<Service> serviceRequestAuthorizer) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return authorizeService(request);
    }

    private Service authorizeService(HttpServletRequest request) {
        try {
            return serviceRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful service authentication");
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
