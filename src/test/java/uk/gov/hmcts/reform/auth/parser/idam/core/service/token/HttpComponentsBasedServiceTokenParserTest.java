package uk.gov.hmcts.reform.auth.parser.idam.core.service.token;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
class HttpComponentsBasedServiceTokenParserTest {

    private HttpComponentsBasedServiceTokenParser client;

    @BeforeEach
    public void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        client = new HttpComponentsBasedServiceTokenParser(
            HttpClients.createMinimal(),
            "http://localhost:" + wmRuntimeInfo.getHttpPort()
        );
    }

//    bad
    @Test
    void happyPath() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("subject"))
        );

        String subject = client.parse("someJwt");

        assertThat(subject).isEqualTo("subject");
    }

//    bad
    @Test
    void bearerShouldNotBePrependedIfItsAlreadyPresent() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("subject"))
        );

        String subject = client.parse("Bearer someJwt");

        assertThat(subject).isEqualTo("subject");
    }

//    Bad
    @Test
    void non2xxResponseShouldResultInException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withBody("subject"))
        );

        assertThrows(ServiceTokenParsingException.class, () -> client.parse("someJwt"));
    }

//    Bad
    @Test
    void response401ShouldResultInServiceTokenInvalidException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer expiredJwt"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withBody("any-body"))
        );

        assertThrows(ServiceTokenInvalidException.class, () -> client.parse("expiredJwt"));
    }
}
