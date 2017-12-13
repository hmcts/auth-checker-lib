package uk.gov.hmcts.reform.auth.checker.core.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenInvalidException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedRoleException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedUserException;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenInvalidException;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParsingException;

public class UserRequestAuthorizer implements RequestAuthorizer<User> {
    public static final String AUTHORISATION = "Authorization";

    private final SubjectResolver<User> userResolver;
    private final Function<HttpServletRequest, Optional<String>> userIdExtractor;
    private final Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor;

    public UserRequestAuthorizer(SubjectResolver<User> userResolver,
                                 Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                 Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor) {
        this.userResolver = userResolver;
        this.userIdExtractor = userIdExtractor;
        this.authorizedRolesExtractor = authorizedRolesExtractor;
    }

    @Override
    public User authorise(HttpServletRequest request) throws UnauthorisedRoleException, UnauthorisedUserException {
        String bearerToken = request.getHeader(AUTHORISATION);
        if (bearerToken == null) {
            throw new BearerTokenMissingException();
        }

        User user = getTokenDetails(bearerToken);

        Collection<String> authorizedRoles = authorizedRolesExtractor.apply(request);
        if (!authorizedRoles.isEmpty() && Collections.disjoint(authorizedRoles, user.getRoles())) {
            throw new UnauthorisedRoleException();
        }

        userIdExtractor.apply(request).ifPresent(resourceUserId -> verifyRequestUserId(resourceUserId, user));

        return user;
    }

    private User getTokenDetails(String bearerToken) {
        try {
            return userResolver.getTokenDetails(bearerToken);
        } catch (UserTokenInvalidException e) {
            throw new BearerTokenInvalidException();
        } catch (UserTokenParsingException e) {
            throw new AuthCheckerException("Error parsing JWT token");
        }
    }

    private void verifyRequestUserId(String requestUserId, User userLoggedIn) throws UnauthorisedUserException {
        if (!requestUserId.equals(userLoggedIn.getPrincipal())) {
            throw new UnauthorisedUserException();
        }
    }
}

