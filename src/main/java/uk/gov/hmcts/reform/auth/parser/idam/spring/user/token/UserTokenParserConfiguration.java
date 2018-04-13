package uk.gov.hmcts.reform.auth.parser.idam.spring.user.token;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.HttpComponentsBasedUserTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenDetails;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParser;

@Lazy
@Configuration
public class UserTokenParserConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "userTokenParserHttpClient")
    public HttpClient userTokenParserHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public UserTokenParser<UserTokenDetails> userTokenParser(HttpClient userTokenParserHttpClient,
                                                             @Value("${auth.idam.client.baseUrl}") String baseUrl) {
        return new HttpComponentsBasedUserTokenParser<>(userTokenParserHttpClient, baseUrl, UserTokenDetails.class);
    }

}
