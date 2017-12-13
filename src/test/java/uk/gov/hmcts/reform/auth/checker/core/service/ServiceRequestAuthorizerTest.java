package uk.gov.hmcts.reform.auth.checker.core.service;


import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedServiceException;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceRequestAuthorizerTest {

    @Test
    public void testWhenAuthorisedService() throws Throwable {
        Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-a"), "service-x", "service-a", "service-z");
        assertThat("Services don't match!", actualService.getPrincipal(), is("service-a"));
    }

    @Test
    public void testWhenAuthorisedServiceCaseInsensitive() throws Throwable {
        Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-A"), "service-x", "SERVICE-a", "service-z");
        assertThat("Services don't match!", actualService.getPrincipal(), is("service-A"));
    }

    @Test(expected = UnauthorisedServiceException.class)
    public void testWhenUnauthorisedService() throws Throwable {
        Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-a"), "service-x", "service-y", "service-z");
        assertThat("Services don't match!", actualService.getPrincipal(), is("service-a"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenNoServicesSpecified() throws Throwable {
        Service actualService = authoriseServiceWhenAllowedPrincipals(new Service("service-a"));
        assertThat("Services don't match!", actualService.getPrincipal(), is("service-a"));
    }

    @Test(expected = BearerTokenMissingException.class)
    public void testWhenNoServiceToken() throws Throwable {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("ServiceAuthorization")).thenReturn(null);

        SubjectResolver<Service> mockSubjectResolver = subjectResolver("Bearer aa.bbbb.cccc", new Service("service-a"));
        ServiceRequestAuthorizer serviceRequestAuthorizer = new ServiceRequestAuthorizer(mockSubjectResolver, (any) -> asList("service-x", "service-a", "service-z"));

        serviceRequestAuthorizer.authorise(mockRequest);
    }

    private Service authoriseServiceWhenAllowedPrincipals(Service service, String... allowedPrincipals) {
        String bearerToken = "Bearer ANY";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("ServiceAuthorization")).thenReturn(bearerToken);

        SubjectResolver<Service> subjectResolver = subjectResolver(bearerToken, service);
        ServiceRequestAuthorizer serviceRequestAuthorizer = new ServiceRequestAuthorizer(subjectResolver, (any) -> asList(allowedPrincipals));

        return serviceRequestAuthorizer.authorise(request);
    }

    private SubjectResolver<Service> subjectResolver(String bearerToken, Service stubbedService) {
        SubjectResolver<Service> mockSubjectResolver = mock(SubjectResolver.class);
        when(mockSubjectResolver.getTokenDetails(bearerToken)).thenReturn(stubbedService);
        return mockSubjectResolver;
    }
}
