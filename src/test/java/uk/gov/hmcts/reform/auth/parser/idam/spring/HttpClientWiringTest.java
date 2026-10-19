package uk.gov.hmcts.reform.auth.parser.idam.spring;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.user.token.UserTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.spring.service.token.ServiceTokenParserConfiguration;
import uk.gov.hmcts.reform.auth.parser.idam.spring.user.token.UserTokenParserConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpClientWiringTest {

    @Test
    void shouldResolveHttpClientsWhenMultipleBeansExist() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("test", Map.of(
                    "auth.provider.service.client.baseUrl", "http://localhost",
                    "auth.idam.client.baseUrl", "http://localhost"
                ))
            );

            context.register(
                ServiceTokenParserConfiguration.class,
                UserTokenParserConfiguration.class
            );

            context.registerBean(
                "httpClient5",
                CloseableHttpClient.class,
                HttpClients::createDefault
            );

            context.refresh();

            assertNotNull(context.getBean(ServiceTokenParser.class));
            assertNotNull(context.getBean(UserTokenParser.class));
        }
    }
}
