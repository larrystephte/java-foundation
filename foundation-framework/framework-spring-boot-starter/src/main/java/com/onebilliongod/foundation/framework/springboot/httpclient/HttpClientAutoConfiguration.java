package com.onebilliongod.foundation.framework.springboot.httpclient;


import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "http.client")
public class HttpClientAutoConfiguration {
    // Configuration properties
    private boolean useOkHttp = true;

    private int connectTimeout = 150;
    private int readTimeout = 2000;
    private int writeTimeout = 500;

    private long keepAliveDuration = 6000;

    private int maxIdleConnections = 5;

    private int maxTotalConnections = 200;


    // OkHttp Client configuration
    @Bean
    @ConditionalOnProperty(name = "http.client.useOkHttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MILLISECONDS);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .readTimeout(Duration.ofMillis(readTimeout))
                .writeTimeout(Duration.ofMillis(writeTimeout))
                .build();
    }

    // Apache HttpClient configuration
    @Bean
    @ConditionalOnProperty(name = "http.client.useOkHttp", havingValue = "false")
    public CloseableHttpClient apacheHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(16); //2^4
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
                .setTimeToLive(Timeout.ofMilliseconds(keepAliveDuration))
                .setValidateAfterInactivity(Timeout.ofSeconds(5))
                .build());

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionKeepAlive(TimeValue.ofMilliseconds(keepAliveDuration))
                        .setRedirectsEnabled(true)
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(writeTimeout))
                        .setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS)
                        .build())
                .build();
    }

    // RestTemplate configuration with conditional selection between OkHttp and Apache
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RestTemplate.class)
    public RestTemplate restTemplate(@Nullable OkHttpClient okHttpClient,
                                     @Nullable CloseableHttpClient apacheHttpClient,
                                     ClientHttpRequestInterceptor globalRequestInterceptor) {
        RestTemplate restTemplate;
        if (useOkHttp) {
            restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
        } else {
            restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(apacheHttpClient));
        }
        restTemplate.setInterceptors(Collections.singletonList(globalRequestInterceptor));
        return restTemplate;
    }

    // Feign client configuration
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Feign.class)
    public Feign.Builder feignBuilder(@Nullable OkHttpClient okHttpClient,
                                      @Nullable CloseableHttpClient apacheHttpClient,
                                      RequestInterceptor feignRequestInterceptor) {
        if (useOkHttp) {
            return Feign.builder().client((Client) okHttpClient)
                    .requestInterceptor(feignRequestInterceptor);
        } else {
            return Feign.builder().client((Client) apacheHttpClient)
                    .requestInterceptor(feignRequestInterceptor);
        }
    }

    // Global Request Interceptor for Feign and RestTemplate
    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestInterceptor globalRequestInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().add("Content-Type", "application/json");
            // Add additional authentication or other information here
            return execution.execute(request, body);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            // Set global headers
            template.header("Content-Type", "application/json");
            // Add additional authentication or other information here
        };
    }
}
