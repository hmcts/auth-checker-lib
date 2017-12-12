package uk.gov.hmcts.auth.checker.spring.backdoors;

import java.util.concurrent.ConcurrentHashMap;
import uk.gov.hmcts.auth.checker.SubjectResolver;
import uk.gov.hmcts.auth.checker.user.User;
import uk.gov.hmcts.auth.idam.user.token.UserTokenParsingException;

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
