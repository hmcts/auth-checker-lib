package uk.gov.hmcts.auth.checker.user;


import uk.gov.hmcts.auth.checker.SubjectResolver;
import uk.gov.hmcts.auth.idam.user.token.UserTokenDetails;
import uk.gov.hmcts.auth.idam.user.token.UserTokenParser;

public class UserResolver implements SubjectResolver<User> {
    private final UserTokenParser userTokenParser;

    public UserResolver(UserTokenParser userTokenParser) {
        this.userTokenParser = userTokenParser;
    }

    @Override
    public User getTokenDetails(String bearerToken) {
        UserTokenDetails details = userTokenParser.parse(bearerToken);
        return new User(details.getId(), details.getRoles());
    }
}
