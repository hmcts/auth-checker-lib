package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokenDetails {
    private final String id;
    private final Set<String> roles;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserTokenDetails(@JsonProperty("id") String id, @JsonProperty("roles")Set<String> roles) {
        this.id = id;
        this.roles = roles;
    }
}
