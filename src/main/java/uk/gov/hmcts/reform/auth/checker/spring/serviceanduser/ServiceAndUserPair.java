package uk.gov.hmcts.reform.auth.checker.spring.serviceanduser;

import lombok.Data;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

@Data
public class ServiceAndUserPair {
    private final Service service;
    private final User user;
}
