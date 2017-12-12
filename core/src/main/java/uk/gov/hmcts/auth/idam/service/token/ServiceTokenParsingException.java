package uk.gov.hmcts.auth.idam.service.token;

public class ServiceTokenParsingException extends RuntimeException {
    public ServiceTokenParsingException() {
    }

    public ServiceTokenParsingException(Throwable cause) {
        super(cause);
    }
}
