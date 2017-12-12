package uk.gov.hmcts.auth.checker.spring.serviceanduser;

import lombok.Data;
import uk.gov.hmcts.auth.checker.service.Service;
import uk.gov.hmcts.auth.checker.user.User;

@Data
public class ServiceAndUserPair {
    private final Service service;
    private final User user;
}
