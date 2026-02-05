package uk.gov.hmcts.reform.auth.checker.spring.serviceonly;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.hmcts.reform.auth.checker.spring.backdoors.ServiceResolverBackdoor;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer.AUTHORISATION;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class ServiceOnlyComponentTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ServiceResolverBackdoor serviceResolverBackdoor;

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
    void unmatchedServiceShouldResultIn401() {
        serviceResolverBackdoor.registerToken("token", "unmatched");

        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "token")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void happyPathResultsIn200() {
        serviceResolverBackdoor.registerToken("token", "divorce");

        webTestClient.get()
            .uri("/test")
            .header(AUTHORISATION, "token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo("divorce");
    }
}
