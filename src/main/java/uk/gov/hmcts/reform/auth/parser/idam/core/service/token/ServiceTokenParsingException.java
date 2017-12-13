package uk.gov.hmcts.reform.auth.parser.idam.core.service.token;

public class ServiceTokenParsingException extends RuntimeException {
    public ServiceTokenParsingException() {
    }

    public ServiceTokenParsingException(Throwable cause) {
        super(cause);
    }
}
