package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteUserTokenDetails {

    private final String defaultService;
    private final String email;
    private final String forename;
    private final String surname;
    private final String id;
    private final Set<String> roles;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CompleteUserTokenDetails(@JsonProperty("defaultService") String defaultService,
                                    @JsonProperty("email") String email,
                                    @JsonProperty("forename") String forename,
                                    @JsonProperty("surname") String surname,
                                    @JsonProperty("id") String id,
                                    @JsonProperty("roles") Set<String> roles) {
        this.defaultService = defaultService;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
        this.id = id;
        this.roles = roles;
    }
}
