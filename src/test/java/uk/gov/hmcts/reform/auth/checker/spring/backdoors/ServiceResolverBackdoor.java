package uk.gov.hmcts.reform.auth.checker.spring.backdoors;

import java.util.concurrent.ConcurrentHashMap;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParsingException;

public class ServiceResolverBackdoor implements SubjectResolver<Service> {
    private final ConcurrentHashMap<String, String> tokenToServiceMap = new ConcurrentHashMap<>();

    @Override
    public Service getTokenDetails(String token) {
        String serviceId = tokenToServiceMap.get(token);

        if (serviceId == null) {
            throw new ServiceTokenParsingException(null);
        }

        return new Service(serviceId);
    }

    public void registerToken(String token, String serviceId) {
        tokenToServiceMap.put(token, serviceId);
    }
}
