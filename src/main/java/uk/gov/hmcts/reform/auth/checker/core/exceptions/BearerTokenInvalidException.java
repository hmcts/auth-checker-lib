package uk.gov.hmcts.reform.auth.checker.core.exceptions;

public class BearerTokenInvalidException extends AuthCheckerException {
    public BearerTokenInvalidException(Throwable cause) {
        super(cause);
    }
}
