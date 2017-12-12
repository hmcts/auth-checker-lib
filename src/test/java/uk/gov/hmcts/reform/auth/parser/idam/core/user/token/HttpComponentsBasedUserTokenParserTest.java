package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableSet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.HttpComponentsBasedUserTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenDetails;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenInvalidException;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParsingException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpComponentsBasedUserTokenParserTest {

    private static final String VALID_RESPONSE = "{ \"id\": \"UserId\", \"roles\": [\"citizen\"] }";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private HttpComponentsBasedUserTokenParser client;

//    bad
    @Before
    public void setUp() throws Exception {
        client = new HttpComponentsBasedUserTokenParser(
            HttpClients.createMinimal(),
            "http://localhost:" + wireMockRule.port()
        );
    }

    @Test
    public void happyPath() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(VALID_RESPONSE))
        );

        UserTokenDetails userTokenDetails = client.parse("someJwt");

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("UserId", ImmutableSet.of("citizen")));
    }

    @Test
    public void bearerShouldNotBePrependedIfItsAlreadyPresent() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(VALID_RESPONSE))
        );

        UserTokenDetails userTokenDetails = client.parse("Bearer someJwt");

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("UserId", ImmutableSet.of("citizen")));
    }

    @Test(expected = UserTokenParsingException.class)
    public void non2xxResponseShouldResultInException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer someJwt"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withBody(VALID_RESPONSE))
        );

        client.parse("someJwt");
    }

    @Test(expected = UserTokenInvalidException.class)
    public void status401ShouldResultInUserTokenInvalidException() {
        stubFor(
            get(urlEqualTo("/details")).withHeader("Authorization", matching("Bearer expiredJwt"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withBody(VALID_RESPONSE))
        );

        client.parse("expiredJwt");
    }
}
