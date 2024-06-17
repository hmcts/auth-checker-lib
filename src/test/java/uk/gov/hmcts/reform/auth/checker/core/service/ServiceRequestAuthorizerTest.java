package uk.gov.hmcts.reform.auth.checker.core.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedServiceException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

class ServiceRequestAuthorizerTest {

    @Test
    void testWhenAuthorisedService() {
        assertServiceIsAuthorized(new Service("service-a"), "service-x", "service-a", "service-z");
    }

    @Test
    void testWhenAuthorisedServiceCaseInsensitive() {
        assertServiceIsAuthorized(new Service("service-A"), "service-x", "SERVICE-a", "service-z");
    }

    @Test
    void testWhenUnauthorisedService() {
        assertThrows(UnauthorisedServiceException.class, () -> {
            Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-a"), "service-x", "service-y", "service-z");
            assertThat("Services don't match!", actualService.getPrincipal(), is("service-a"));
        });
    }

    @Test
    void testWhenNoServicesSpecified() {
        assertThrows(IllegalArgumentException.class, () -> {
            Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-a"));
            assertThat("Services don't match!", actualService.getPrincipal(), is("service-a"));
        });
    }

    @Test
    void testWhenNoServiceToken() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("ServiceAuthorization")).thenReturn(null);

        SubjectResolver<Service> mockSubjectResolver = subjectResolver("Bearer aa.bbbb.cccc", new Service("service-a"));
        ServiceRequestAuthorizer serviceRequestAuthorizer = new ServiceRequestAuthorizer(mockSubjectResolver, any -> Arrays.asList("service-x", "service-a", "service-z"));

        assertThrows(BearerTokenMissingException.class, () -> serviceRequestAuthorizer.authorise(mockRequest));
    }

    private void assertServiceIsAuthorized(Service service, String... allowedPrincipals) {
        Service actualService = authoriseServiceWhenAllowedPrincipals(service, allowedPrincipals);
        assertThat("Services don't match!", actualService.getPrincipal(), is(service.getPrincipal()));
    }

    private Service authoriseServiceWhenAllowedPrincipals(Service service, String... allowedPrincipals) {
        String bearerToken = "Bearer ANY";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("ServiceAuthorization")).thenReturn(bearerToken);

        SubjectResolver<Service> subjectResolver = subjectResolver(bearerToken, service);
        ServiceRequestAuthorizer serviceRequestAuthorizer = new ServiceRequestAuthorizer(subjectResolver, any -> Arrays.asList(allowedPrincipals));

        return serviceRequestAuthorizer.authorise(request);
    }

    private SubjectResolver<Service> subjectResolver(String bearerToken, Service stubbedService) {
        SubjectResolver<Service> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(bearerToken)).thenReturn(stubbedService);
        return mockSubjectResolver;
    }
}
