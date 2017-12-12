package uk.gov.hmcts.reform.auth.checker.core.user;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import uk.gov.hmcts.reform.auth.checker.core.Subject;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedRoleException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedUserException;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Sets.newSet;

public class UserRequestAuthorizerTest {

    private String userId = "1111-2222";
    private Function<HttpServletRequest, Optional<String>> extractUserIdFromRequest = (String) -> Optional.of(userId);

    @Test
    public void testWhenAuthorisedRoleAndValidUserId() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c", "service-a"));

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat((User) actualSubject, is(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test
    public void testWhenAuthorisedRoleAndNoUserIdInRequest() throws Throwable {

        Function<HttpServletRequest, Optional<String>> noUserIdInRequest = (String) -> Optional.empty();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, noUserIdInRequest, (any) -> asList("role-a", "role-b", "role-c", "service-a"));

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat((User) actualSubject, is(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test
    public void testWhenNoGlobalRoles() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> new ArrayList<>());

        Subject actualSubject = userRequestAuthorizer.authorise(mockRequest);

        assertThat((User) actualSubject, is(User.class));
        assertThat("Subjects don't match!", actualSubject, is(stubbedSubject));
    }

    @Test(expected = UnauthorisedRoleException.class)
    public void testWhenRoleNotAuthorised() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User(userId, newSet("role-z", "role-x"));

        SubjectResolver mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, null, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }

    @Test(expected = UnauthorisedUserException.class)
    public void testWhenAuthorisedRoleAndInValidUserId() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        User stubbedSubject = new User("SomeOtherUser", newSet("role-z", "role-c"));

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenReturn(stubbedSubject);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenInvalidBearerToken() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new IllegalArgumentException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }

    @Test(expected = BearerTokenMissingException.class)
    public void testWhenMissingBearerToken() throws Throwable {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenNullGetUserIdFunction() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new IllegalArgumentException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, null, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }

    @Test(expected = AuthenticationProviderUnavailableException.class)
    public void testWhenAuthenticationProviderNotAvailable() throws Throwable {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String userBearerToken = "Bearer aa.bbbb.cccc";
        when(mockRequest.getHeader("Authorization")).thenReturn(userBearerToken);

        SubjectResolver<User> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(userBearerToken)).thenThrow(new AuthenticationProviderUnavailableException());

        UserRequestAuthorizer userRequestAuthorizer = new UserRequestAuthorizer(mockSubjectResolver, extractUserIdFromRequest, (any) -> asList("role-a", "role-b", "role-c"));

        userRequestAuthorizer.authorise(mockRequest);
    }
}
