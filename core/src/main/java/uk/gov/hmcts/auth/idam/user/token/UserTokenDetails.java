package uk.gov.hmcts.auth.idam.user.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokenDetails {
    private final String id;
    private final Set<String> roles;
}
