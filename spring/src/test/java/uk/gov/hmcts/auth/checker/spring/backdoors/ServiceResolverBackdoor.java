package uk.gov.hmcts.auth.checker.spring.backdoors;

import java.util.concurrent.ConcurrentHashMap;
import uk.gov.hmcts.auth.checker.service.Service;
import uk.gov.hmcts.auth.checker.SubjectResolver;
import uk.gov.hmcts.auth.idam.service.token.ServiceTokenParsingException;

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
