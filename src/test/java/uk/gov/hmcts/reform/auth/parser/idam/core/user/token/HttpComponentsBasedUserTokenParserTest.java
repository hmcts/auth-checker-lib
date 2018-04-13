package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableSet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpComponentsBasedUserTokenParserTest {

    private static final String VALID_RESPONSE = "{\n" +
        "        \"defaultService\" : \"BAR\",\n" +
        "        \"email\" : \"post.clerk@hmcts.net\",\n" +
        "        \"forename\" : \"Chris\",\n" +
        "        \"id\" : 365750,\n" +
        "        \"roles\" : [\"bar-post-clerk\"],\n" +
        "        \"surname\" : \"Spencer\"\n" +
        "    }";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private HttpComponentsBasedUserTokenParser<UserTokenDetails> client;
    private HttpComponentsBasedUserTokenParser<CompleteUserTokenDetails> fullClient;

    @Before
    public void setUp() throws Exception {
        client = new HttpComponentsBasedUserTokenParser<>(
            HttpClients.createMinimal(),
            "http://localhost:" + wireMockRule.port(),
            UserTokenDetails.class
        );

        fullClient = new HttpComponentsBasedUserTokenParser<>(
            HttpClients.createMinimal(),
            "http://localhost:" + wireMockRule.port(),
            CompleteUserTokenDetails.class
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

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("365750", ImmutableSet.of("bar-post-clerk")));
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

        assertThat(userTokenDetails).isEqualTo(new UserTokenDetails("365750", ImmutableSet.of("bar-post-clerk")));
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

    @Test
    public void testFullUserRetrieval() {
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
