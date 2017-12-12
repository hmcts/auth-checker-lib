package uk.gov.hmcts.auth.checker.spring.serviceonly;

import java.util.Collections;

public class ServiceDetails extends org.springframework.security.core.userdetails.User {

    public ServiceDetails(String servicename) {
        super(servicename, "N/A", Collections.emptyList());
    }

}
