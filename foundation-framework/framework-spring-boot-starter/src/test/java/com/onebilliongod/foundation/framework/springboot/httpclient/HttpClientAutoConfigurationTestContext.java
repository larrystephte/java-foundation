package com.onebilliongod.foundation.framework.springboot.httpclient;

import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class HttpClientAutoConfigurationTestContext {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

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
        });
    }
}
