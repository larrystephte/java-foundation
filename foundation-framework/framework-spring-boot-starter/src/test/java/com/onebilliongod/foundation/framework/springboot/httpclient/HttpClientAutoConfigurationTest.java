package com.onebilliongod.foundation.framework.springboot.httpclient;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = HttpClientAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "http.client.useOkHttp=true")
public class HttpClientAutoConfigurationTest {

    @Test
    public void testOkHttpClientBeanLoaded() {
        OkHttpClient okHttpClient = new HttpClientAutoConfiguration().okHttpClient();
        assertNotNull(okHttpClient, "OkHttpClient should be loaded when http.client.useOkHttp is true");
    }
}
