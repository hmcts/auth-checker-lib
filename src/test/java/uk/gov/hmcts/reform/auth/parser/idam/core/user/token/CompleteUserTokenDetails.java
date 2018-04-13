package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteUserTokenDetails {

    private final String defaultService;
    private final String email;
    private final String forename;
    private final String surname;
    private final String id;
    private final Set<String> roles;
}
