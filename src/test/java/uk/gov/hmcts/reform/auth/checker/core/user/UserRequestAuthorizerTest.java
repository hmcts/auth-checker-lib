package uk.gov.hmcts.reform.auth.checker.core.user;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.auth.checker.core.Subject;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedRoleException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedUserException;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Sets.newSet;

class UserRequestAuthorizerTest {

    private final String userId = "1111-2222";
    private final Function<HttpServletRequest, Optional<String>> extractUserIdFromRequest = (String) -> Optional.of(userId);

    @Test
    void testWhenAuthorisedRoleAndValidUserId() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c", "service-a"));

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat(actualSubject, instanceOf(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test
    void testWhenAuthorisedRoleAndNoUserIdInRequest() {

        Function<HttpServletRequest, Optional<String>> noUserIdInRequest = (String) -> Optional.empty();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, noUserIdInRequest, (any) -> asList("role-a", "role-b", "role-c", "service-a"));

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat((User) actualSubject, instanceOf(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test
    void testWhenNoGlobalRoles() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> new ArrayList<>());

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat((User) actualSubject, instanceOf(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test
    void testWhenRoleNotAuthorised() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-x"));

        SubjectResolver mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, null, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(UnauthorisedRoleException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }

    @Test
    void testWhenAuthorisedRoleAndInValidUserId() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User("SomeOtherUser", newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(UnauthorisedUserException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }

    @Test
    void testWhenInvalidBearerToken() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new IllegalArgumentException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(IllegalArgumentException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }

    @Test
    void testWhenMissingBearerToken() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(BearerTokenMissingException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }

    @Test
    void testWhenNullGetUserIdFunction() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new IllegalArgumentException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, null, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(IllegalArgumentException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }

    @Test
    void testWhenAuthenticationProviderNotAvailable() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new AuthenticationProviderUnavailableException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        assertThrows(AuthenticationProviderUnavailableException.class, () -> userRequestAuthorizer.authorise(mockRequest));
    }
}
