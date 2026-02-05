package uk.gov.hmcts.reform.auth.checker.spring.serviceanduser;

import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.UserResolverBackdoor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ServiceAndUserComponentTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserResolverBackdoor userResolverBackdoor;

    @Autowired
    private ServiceResolverBackdoor serviceResolverBackdoor;

    @Test
    void noAuthorizationHeadersShouldResultIn403() {
        webTestClient.get()
            .uri("/test")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void noUserAuthorizationHeaderShouldResultIn403() {
        userResolverBackdoor.registerToken(
            "userToken",
            new User("user", Sets.newSet("citizen"))
        );

        webTestClient.get()
            .uri("/test")
            .header(ServiceRequestAuthorizer.AUTHORISATION, "unknownServiceToken")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void noServiceAuthorizationHeaderShouldResultIn403() {
        serviceResolverBackdoor.registerToken("serviceToken", "divorce");

        webTestClient.get()
            .uri("/test")
            .header(UserRequestAuthorizer.AUTHORISATION, "unknownUserToken")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void happyPathResultsIn200() {
        userResolverBackdoor.registerToken(
            "userToken",
            new User("user", Sets.newSet("citizen"))
        );
        serviceResolverBackdoor.registerToken("serviceToken", "divorce");

        webTestClient.get()
            .uri("/test")
            .header(ServiceRequestAuthorizer.AUTHORISATION, "serviceToken")
            .header(UserRequestAuthorizer.AUTHORISATION, "userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo("user@divorce");
    }
}
