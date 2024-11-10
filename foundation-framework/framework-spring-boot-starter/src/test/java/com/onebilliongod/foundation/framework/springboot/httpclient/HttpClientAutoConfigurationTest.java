package com.onebilliongod.foundation.framework.springboot.httpclient;

import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = HttpClientAutoConfiguration.class)
@ActiveProfiles("test")
//@TestPropertySource(properties = "http.client.useOkHttp=false")
public class HttpClientAutoConfigurationTest {
    @Autowired(required = false)
    private OkHttpClient okHttpClient;

    @Autowired(required = false)
    private CloseableHttpClient httpClient;


    @Test
    public void testOkHttpClientBeanLoaded() {
        assertNotNull(okHttpClient, "OkHttpClient should be loaded when http.client.useOkHttp is true");
    }

    @Test
    public void testHttpClientBeanLoaded() {
        assertNotNull(httpClient, "OkHttpClient should be loaded when http.client.useOkHttp is true");
    }
}
