package com.onebilliongod.foundation.framework.springboot.httpclient;

import feign.Feign;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class HttpClientAutoConfigurationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class,
                    FeignAutoConfiguration.class));

    @Test
    void httpClientShouldBeConfiguredWithProperties() {
        this.contextRunner.withPropertyValues("http.client.useOkHttp=false").run((context) -> {
            // Verify that httpClient exists
            assertThat(context).hasSingleBean(OkHttpClient.class);
//            HttpClient httpClient = context.getBean(HttpClient.class);
//            assertThat(httpClient.connectTimeout().get().toMillis()).isEqualTo(5000);
        });
    }

    @Test
    void okhttpShouldBeConfiguredWithProperties() {
        this.contextRunner.withPropertyValues("http.client.useOkHttp=true",
                "http.client.connectTimeout=501").run((context) -> {
            // Verify that OkHttpClient exists
            assertThat(context).hasSingleBean(OkHttpClient.class);
            OkHttpClient okHttpClient = context.getBean(OkHttpClient.class);
            assertThat(okHttpClient.connectTimeoutMillis()).isEqualTo(501);
            assertThat(okHttpClient.connectionPool().connectionCount()).isEqualTo(0);
            assertThat(okHttpClient.connectionPool().idleConnectionCount()).isEqualTo(0);

        });
    }

    @Test
    void testRestTemplateAutoConfiguration() {
        this.contextRunner.withPropertyValues("http.client.useOkHttp=true").run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);

            RestTemplate restTemplate = context.getBean(RestTemplate.class);

            // Assert that the request factory is wrapped by InterceptingClientHttpRequestFactory
            assertThat(restTemplate.getRequestFactory())
                    .isInstanceOf(InterceptingClientHttpRequestFactory.class);
        });
    }

    @Test
    void testFeignTemplateAutoConfiguration() {
        this.contextRunner.withPropertyValues("http.client.useOkHttp=true").run(context -> {
            assertThat(context).hasSingleBean(Feign.Builder.class);

            Feign.Builder feignBuild = context.getBean(Feign.Builder.class);
            assertThat(feignBuild).isNotNull();
        });
    }
}
