package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.collect.ImmutableSet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
class HttpComponentsBasedUserTokenParserTest {

    private static final String VALID_RESPONSE = """
            {
                    "defaultService" : "BAR",
                    "email" : "post.clerk@hmcts.net",
                    "forename" : "Chris",
                    "id" : 365750,
                    "roles" : ["bar-post-clerk"],
                    "surname" : "Spencer"
                }""";

    private HttpComponentsBasedUserTokenParser<UserTokenDetails> client;
    private HttpComponentsBasedUserTokenParser<CompleteUserTokenDetails> fullClient;

    @BeforeEach
    public void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        client = new HttpComponentsBasedUserTokenParser<>(
            HttpClients.createMinimal(),
            "http://localhost:" + wmRuntimeInfo.getHttpPort(),
            UserTokenDetails.class
        );

        fullClient = new HttpComponentsBasedUserTokenParser<>(
            HttpClients.createMinimal(),
            "http://localhost:" + wmRuntimeInfo.getHttpPort(),
            CompleteUserTokenDetails.class
        );
    }

    @Test
    void happyPath() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(VALID_RESPONSE))
        );

        UserTokenDetails userTokenDetails = client.parse("someJwt");

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("365750", ImmutableSet.of("bar-post-clerk")));
    }

    @Test
    void bearerShouldNotBePrependedIfItsAlreadyPresent() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(VALID_RESPONSE))
        );

        UserTokenDetails userTokenDetails = client.parse("Bearer someJwt");

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("365750", ImmutableSet.of("bar-post-clerk")));
    }

    @Test
    void non2xxResponseShouldResultInException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withBody(VALID_RESPONSE))
        );

        assertThrows(UserTokenParsingException.class, () -> client.parse("someJwt"));
    }

    @Test
    void status401ShouldResultInUserTokenInvalidException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer expiredJwt"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withBody(VALID_RESPONSE))
        );

        assertThrows(UserTokenInvalidException.class, () -> client.parse("expiredJwt"));
    }

    @Test
    void testFullUserRetrieval() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(VALID_RESPONSE))
        );

        CompleteUserTokenDetails userTokenDetails = fullClient.parse("someJwt");
        assertThat(userTokenDetails).isEqualTo(
            new CompleteUserTokenDetails("BAR", "post.clerk@hmcts.net", "Chris",
                "Spencer", "365750", ImmutableSet.of("bar-post-clerk"))
        );
    }
}
