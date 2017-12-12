package uk.gov.hmcts.auth.checker.service;


import uk.gov.hmcts.auth.checker.SubjectResolver;
import uk.gov.hmcts.auth.idam.service.token.ServiceTokenParser;

public class ServiceResolver implements SubjectResolver<Service> {

    private final ServiceTokenParser serviceTokenParser;

    public ServiceResolver(ServiceTokenParser serviceTokenParser) {
        this.serviceTokenParser = serviceTokenParser;
    }

    @Override
    public Service getTokenDetails(String bearerToken) {
        String subject = serviceTokenParser.parse(bearerToken);
        return new Service(subject);
    }
}
