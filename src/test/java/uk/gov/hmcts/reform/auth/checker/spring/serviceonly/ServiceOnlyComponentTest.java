package uk.gov.hmcts.reform.auth.checker.spring.serviceonly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.ServiceResolverBackdoor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer.AUTHORISATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ServiceOnlyComponentTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServiceResolverBackdoor serviceResolverBackdoor;

    @Test
    void noAuthorizationHeaderShouldResultIn403() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void unknownTokenShouldResultIn401() {
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withServiceHeader("unknownToken"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void unmatchedServiceShouldResultIn401() {
        serviceResolverBackdoor.registerToken("token", "unmatched");
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withServiceHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void happyPathResultsIn200() {
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
