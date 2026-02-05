package uk.gov.hmcts.reform.auth.checker.spring.useronly;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.UserResolverBackdoor;

import java.util.Set;

import static uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer.AUTHORISATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserOnlyComponentTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserResolverBackdoor userResolverBackdoor;

    @Test
    void noAuthorizationHeaderShouldResultIn403() {
        webTestClient.get()
            .uri("/test")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void unknownTokenShouldResultIn401() {
        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "unknownToken")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void unmatchedRoleShouldResultIn401() {
        userResolverBackdoor.registerToken(
            "token",
            new User("1", Set.of("unmatched"))
        );

        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "token")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void unmatchedUserIdShouldResultIn401() {
        userResolverBackdoor.registerToken(
            "token",
            new User("2", Set.of("citizen"))
        );

        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "token")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void happyPathResultsIn200() {
        userResolverBackdoor.registerToken(
            "token",
            new User("1", Set.of("citizen"))
        );

        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo("1");
    }
}
