package uk.gov.hmcts.auth.idam.service.token;

public interface ServiceTokenParser {
    String parse(String jwt) throws ServiceTokenParsingException;
}
