package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public class HttpComponentsBasedUserTokenParser implements UserTokenParser {

    private final HttpClient httpClient;
    private final String baseUrl;
//bad
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpComponentsBasedUserTokenParser(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    @SuppressWarnings(value = "HTTP_PARAMETER_POLLUTION", justification = "baseUrl + /details is not based on user input")
    public UserTokenDetails parse(String jwt) {
        try {
            String bearerJwt = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
            HttpGet request = new HttpGet(baseUrl + "/details");
            request.addHeader("Authorization", bearerJwt);

            return httpClient.execute(request, httpResponse -> {
                checkStatusIs2xx(httpResponse);
                return objectMapper.readValue(httpResponse.getEntity().getContent(), UserTokenDetails.class);
            });
        } catch (IOException e) {
            throw new UserTokenParsingException(e);
        }
    }

    private void checkStatusIs2xx(HttpResponse httpResponse) throws IOException {
        int status = httpResponse.getStatusLine().getStatusCode();

        if (status == 401) {
            throw new UserTokenInvalidException();
        }

        if (status < 200 || status >= 300) {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}
