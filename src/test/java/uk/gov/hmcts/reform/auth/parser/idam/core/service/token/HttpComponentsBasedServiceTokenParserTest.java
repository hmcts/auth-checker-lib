package uk.gov.hmcts.reform.auth.parser.idam.core.service.token;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpComponentsBasedServiceTokenParserTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private HttpComponentsBasedServiceTokenParser client;

    @Before
    public void setUp() throws Exception {
        client = new HttpComponentsBasedServiceTokenParser(
            HttpClients.createMinimal(),
            "http://localhost:" + wireMockRule.port()
        );
    }

//    bad
    @Test
    public void happyPath() {
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
    public void bearerShouldNotBePrependedIfItsAlreadyPresent() {
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
    @Test(expected = ServiceTokenParsingException.class)
    public void non2xxResponseShouldResultInException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withBody("subject"))
        );

        client.parse("someJwt");
    }

//    Bad
    @Test(expected = ServiceTokenInvalidException.class)
    public void response401ShouldResultInServiceTokenInvalidException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer expiredJwt"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withBody("any-body"))
        );

        client.parse("expiredJwt");
    }
}
