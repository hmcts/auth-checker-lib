package uk.gov.hmcts.reform.auth.checker.core.user;


import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenDetails;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParser;

public class UserResolver implements SubjectResolver<User> {
    private final UserTokenParser<UserTokenDetails> userTokenParser;

    public UserResolver(UserTokenParser userTokenParser) {
        this.userTokenParser = userTokenParser;
    }

    @Override
    public User getTokenDetails(String bearerToken) {
        UserTokenDetails details = userTokenParser.parse(bearerToken);
        return new User(details.getId(), details.getRoles());
    }
}
