package uk.gov.hmcts.reform.auth.parser.idam.spring.service.token;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.HttpComponentsBasedServiceTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;

@Configuration
@Lazy
public class ServiceTokenParserConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serviceTokenParserHttpClient")
    public CloseableHttpClient serviceTokenParserHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public ServiceTokenParser serviceAuthProviderAuthCheckClient(
        @Qualifier("serviceTokenParserHttpClient") CloseableHttpClient serviceTokenParserHttpClient,
        @Value("${auth.provider.service.client.baseUrl}") String baseUrl) {
            return new HttpComponentsBasedServiceTokenParser(serviceTokenParserHttpClient, baseUrl);
    }

}
