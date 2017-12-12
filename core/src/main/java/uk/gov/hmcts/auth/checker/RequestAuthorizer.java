package uk.gov.hmcts.auth.checker;

import javax.servlet.http.HttpServletRequest;

public interface RequestAuthorizer<T extends Subject> {
    T authorise(HttpServletRequest request);
}
