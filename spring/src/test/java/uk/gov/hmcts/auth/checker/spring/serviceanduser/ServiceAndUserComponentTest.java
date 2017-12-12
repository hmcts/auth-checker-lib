package uk.gov.hmcts.auth.checker.spring.serviceanduser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.auth.checker.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.auth.checker.user.User;
import uk.gov.hmcts.auth.checker.user.UserRequestAuthorizer;
import uk.gov.hmcts.auth.checker.spring.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.auth.checker.spring.backdoors.UserResolverBackdoor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ServiceAndUserComponentTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserResolverBackdoor userResolverBackdoor;

    @Autowired
    private ServiceResolverBackdoor serviceResolverBackdoor;

    @Test
    public void noAuthorizationHeadersShouldResultIn403() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void noUserAuthorizationHeaderShouldResultIn403() {
        userResolverBackdoor.registerToken("userToken", new User("user", Sets.newSet("citizen")));
        HttpEntity<Object> entity = withServiceAndUserHeaders("unknownServiceToken", "userToken");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void noServiceAuthorizationHeaderShouldResultIn403() {
        serviceResolverBackdoor.registerToken("serviceToken", "divorce");
        HttpEntity<Object> entity = withServiceAndUserHeaders("serviceToken", "unknownUserToken");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void happyPathResultsIn200() {
        userResolverBackdoor.registerToken("userToken", new User("user", Sets.newSet("citizen")));
        serviceResolverBackdoor.registerToken("serviceToken", "divorce");
        HttpEntity<Object> entity = withServiceAndUserHeaders("serviceToken", "userToken");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("user@divorce");
    }

    private HttpEntity<Object> withServiceAndUserHeaders(String serviceToken, String userToken) {
        HttpHeaders headers = new HttpHeaders();

        if (serviceToken != null) {
            headers.add(ServiceRequestAuthorizer.AUTHORISATION, serviceToken);
        }

        if (userToken != null) {
            headers.add(UserRequestAuthorizer.AUTHORISATION, userToken);
        }

        return new HttpEntity<>(headers);
    }
}
