package uk.gov.hmcts.reform.auth.parser.idam.core.service.token;

public interface ServiceTokenParser {
    String parse(String jwt) throws ServiceTokenParsingException;
}
