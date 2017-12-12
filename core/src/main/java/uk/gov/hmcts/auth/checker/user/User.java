package uk.gov.hmcts.auth.checker.user;

import java.util.Set;
import uk.gov.hmcts.auth.checker.Subject;

public class User extends Subject {

    private final Set<String> roles;

    public User(String principleId, Set<String> roles) {
        super(principleId);
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
