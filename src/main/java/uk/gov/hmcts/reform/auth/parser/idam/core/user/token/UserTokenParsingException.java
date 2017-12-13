package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

public class UserTokenParsingException extends RuntimeException {
    public UserTokenParsingException() {
    }

    public UserTokenParsingException(Throwable cause) {
        super(cause);
    }
}
