package preponderous.viron.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Attaches the configured bearer token (service.authToken) to outgoing requests
 * made by the in-repo client SDKs. If no token is configured, requests are sent
 * unmodified.
 */
public class AuthTokenInterceptor implements ClientHttpRequestInterceptor {
    private final ServiceConfig serviceConfig;

    public AuthTokenInterceptor(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String authToken = serviceConfig.getAuthToken();
        if (StringUtils.hasText(authToken)) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        }
        return execution.execute(request, body);
    }
}
