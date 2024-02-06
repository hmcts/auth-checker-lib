package uk.gov.hmcts.reform.auth.checker.core;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestAuthorizer<T extends Subject> {
    T authorise(HttpServletRequest request);
}
