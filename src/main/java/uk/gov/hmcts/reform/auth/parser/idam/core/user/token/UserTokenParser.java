package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

public interface UserTokenParser {
    UserTokenDetails parse(String jwt) throws UserTokenParsingException;
}
