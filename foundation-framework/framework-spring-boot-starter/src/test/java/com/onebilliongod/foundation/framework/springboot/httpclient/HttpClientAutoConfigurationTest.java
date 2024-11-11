package com.onebilliongod.foundation.framework.springboot.httpclient;

import com.onebilliongod.foundation.framework.springboot.environment.FrameworkEnvironmentPostProcessor;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest()
@ActiveProfiles("test")
//@TestPropertySource(properties = "http.client.useOkHttp=false")
public class HttpClientAutoConfigurationTest {
    @Autowired(required = false)
    private OkHttpClient okHttpClient;

    @Autowired(required = false)
    private CloseableHttpClient httpClient;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private ClientHttpRequestInterceptor globalRequestInterceptor;

    @Mock
    private HttpRequest mockRequest;

    @Mock
    private ClientHttpRequestExecution mockExecution;

    @Mock
    private ClientHttpResponse mockResponse;


    @Test
    public void testOkHttpClientBeanLoaded() {
        assertNotNull(okHttpClient, "OkHttpClient should be loaded when http.client.useOkHttp is true");
    }

    @Test
    public void testHttpClientBeanLoaded() {
        assertNotNull(httpClient, "OkHttpClient should be loaded when http.client.useOkHttp is true");
    }

    @Test
    public void testRestTemplate() {
        assertNotNull(restTemplate, "restTemplate should be loaded when RestTemplate is on classpath");
    }

    @Test
    public void testClientHttpRequestInterceptor() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        when(mockRequest.getHeaders()).thenReturn(headers);
        when(mockRequest.getMethod()).thenReturn(HttpMethod.GET);
        when(mockExecution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(mockResponse);

        byte[] body = "request body".getBytes();

        ClientHttpResponse response = globalRequestInterceptor.intercept(mockRequest, body, mockExecution);

        // Assert
        assertNotNull(response);
        assertEquals(mockResponse, response);
        assertTrue(headers.containsKey("X-Foundation-Origin-Project"));
        assertEquals("default", headers.getFirst("X-Foundation-Origin-Cloud"));

        // Verify that the execute method is called with the mock request and body
        verify(mockExecution, times(1)).execute(eq(mockRequest), eq(body));
    }


}
