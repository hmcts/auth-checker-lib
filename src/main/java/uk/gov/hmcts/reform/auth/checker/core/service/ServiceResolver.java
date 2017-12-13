package uk.gov.hmcts.reform.auth.checker.core.service;


import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;

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
