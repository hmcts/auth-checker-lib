package uk.gov.hmcts.reform.auth.checker.spring.backdoors;

import java.util.concurrent.ConcurrentHashMap;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParsingException;

public class UserResolverBackdoor implements SubjectResolver<User> {
    private final ConcurrentHashMap<String, User> tokenToUserMap = new ConcurrentHashMap<>();

    @Override
    public User getTokenDetails(String token) {
        User user = tokenToUserMap.get(token);

        if (user == null) {
            throw new UserTokenParsingException(null);
        }

        return user;
    }

    public void registerToken(String token, User user) {
        tokenToUserMap.put(token, user);
    }
}
