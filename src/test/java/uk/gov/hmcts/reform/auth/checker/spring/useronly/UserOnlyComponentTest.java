package uk.gov.hmcts.reform.auth.checker.spring.useronly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.UserResolverBackdoor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer.AUTHORISATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserOnlyComponentTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserResolverBackdoor userResolverBackdoor;

    @Test
    void noAuthorizationHeaderShouldResultIn403() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void unknownTokenShouldResultIn401() {
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withUserHeader("unknownToken"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void unmatchedRoleShouldResultIn401() {
        userResolverBackdoor.registerToken("token", new User("1", Sets.newSet("unmatched")));
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withUserHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void unmatchedUserIdShouldResultIn401() {
        userResolverBackdoor.registerToken("token", new User("2", Sets.newSet("citizen")));
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withUserHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void happyPathResultsIn200() {
        userResolverBackdoor.registerToken("token", new User("1", Sets.newSet("citizen")));
        ResponseEntity<String> response = restTemplate.exchange("/test", GET, withUserHeader("token"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("1");
    }

    private HttpEntity<Object> withUserHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORISATION, token);
        return new HttpEntity<>(headers);
    }
}
