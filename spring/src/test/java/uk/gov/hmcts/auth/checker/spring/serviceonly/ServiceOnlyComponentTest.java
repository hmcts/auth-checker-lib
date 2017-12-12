package uk.gov.hmcts.auth.checker.spring.serviceonly;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.auth.checker.spring.backdoors.ServiceResolverBackdoor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.auth.checker.service.ServiceRequestAuthorizer.AUTHORISATION;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ServiceOnlyComponentTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServiceResolverBackdoor serviceResolverBackdoor;

    @Test
    public void noAuthorizationHeaderShouldResultIn403() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void unknownTokenShouldResultIn401() {
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withServiceHeader("unknownToken"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void unmatchedServiceShouldResultIn401() {
        serviceResolverBackdoor.registerToken("token", "unmatched");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withServiceHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void happyPathResultsIn200() {
        serviceResolverBackdoor.registerToken("token", "divorce");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withServiceHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("divorce");
    }

    private HttpEntity<Object> withServiceHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORISATION, token);
        return new HttpEntity<>(headers);
    }
}
