package uk.gov.hmcts.auth.idam.user.token.spring;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.auth.idam.user.token.HttpComponentsBasedUserTokenParser;
import uk.gov.hmcts.auth.idam.user.token.UserTokenParser;

@Lazy
@Configuration
public class UserTokenParserConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "userTokenParserHttpClient")
    public HttpClient userTokenParserHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public UserTokenParser userTokenParser(HttpClient userTokenParserHttpClient,
                                           @Value("${auth.idam.client.baseUrl}") String baseUrl) {
        return new HttpComponentsBasedUserTokenParser(userTokenParserHttpClient, baseUrl);
    }

}
