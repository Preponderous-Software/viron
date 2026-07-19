package preponderous.viron.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthTokenInterceptorTest {
    private ServiceConfig serviceConfig;
    private HttpRequest request;
    private ClientHttpRequestExecution execution;
    private byte[] body;

    @BeforeEach
    void setUp() {
        serviceConfig = new ServiceConfig();
        request = mock(HttpRequest.class);
        execution = mock(ClientHttpRequestExecution.class);
        body = new byte[0];
        when(request.getHeaders()).thenReturn(new HttpHeaders());
    }

    @Test
    void testAddsBearerTokenWhenConfigured() throws Exception {
        serviceConfig.setAuthToken("test-token");
        AuthTokenInterceptor interceptor = new AuthTokenInterceptor(serviceConfig);
        ClientHttpResponse expectedResponse = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(expectedResponse);

        ClientHttpResponse response = interceptor.intercept(request, body, execution);

        assertEquals("Bearer test-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        verify(execution).execute(request, body);
        assertEquals(expectedResponse, response);
    }

    @Test
    void testOmitsHeaderWhenNoTokenConfigured() throws Exception {
        AuthTokenInterceptor interceptor = new AuthTokenInterceptor(serviceConfig);
        when(execution.execute(any(), any())).thenReturn(mock(ClientHttpResponse.class));

        interceptor.intercept(request, body, execution);

        assertNull(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
}
