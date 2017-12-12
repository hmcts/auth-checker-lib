package uk.gov.hmcts.auth.idam.user.token;

public class UserTokenParsingException extends RuntimeException {
    public UserTokenParsingException() {
    }

    public UserTokenParsingException(Throwable cause) {
        super(cause);
    }
}
