package uk.gov.hmcts.reform.auth.parser.idam.core.service.token;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class HttpComponentsBasedServiceTokenParser implements ServiceTokenParser {

    private final CloseableHttpClient httpClient;
    private final String baseUrl;

    public HttpComponentsBasedServiceTokenParser(CloseableHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @SuppressFBWarnings(value = "HTTP_PARAMETER_POLLUTION", justification = "baseUrl + /details is not based on user input")
    public String parse(String jwt) {
        try {
            String bearerJwt = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
            HttpGet request = new HttpGet(baseUrl + "/details");
            request.addHeader("Authorization", bearerJwt);

            return httpClient.execute(request, httpResponse -> {
                checkStatusIs2xx(httpResponse);
                return EntityUtils.toString(httpResponse.getEntity());
            });
        } catch (IOException e) {
            throw new ServiceTokenParsingException(e);
        }
    }

    private void checkStatusIs2xx(HttpResponse httpResponse) throws IOException {
        int status = httpResponse.getCode();

        if (status == 401) {
            throw new ServiceTokenInvalidException();
        }

        if (status < 200 || status >= 300) {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}
