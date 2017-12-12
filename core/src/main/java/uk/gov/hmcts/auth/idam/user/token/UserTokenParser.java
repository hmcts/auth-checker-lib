package uk.gov.hmcts.auth.idam.user.token;

public interface UserTokenParser {
    UserTokenDetails parse(String jwt) throws UserTokenParsingException;
}
