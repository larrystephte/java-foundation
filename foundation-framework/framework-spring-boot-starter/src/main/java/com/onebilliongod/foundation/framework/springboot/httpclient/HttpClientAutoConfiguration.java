package com.onebilliongod.foundation.framework.springboot.httpclient;


import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties({CommonHttpClientConfiguration.class})
//@ConfigurationProperties(prefix = "http.client")
public class HttpClientAutoConfiguration {
    private final CommonHttpClientConfiguration commonConfig;

    public HttpClientAutoConfiguration(CommonHttpClientConfiguration commonConfig) {
        this.commonConfig = commonConfig;
    }

    // OkHttp Client configuration
    @Bean
    @ConditionalOnProperty(name = "http.client.useOkHttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient() {
        // Create a connection pool with specified max idle connections and keep-alive duration
        ConnectionPool connectionPool = new ConnectionPool(commonConfig.getMaxIdleConnections(), commonConfig.getKeepAliveDuration(), TimeUnit.MILLISECONDS);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(Duration.ofMillis(commonConfig.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(commonConfig.getReadTimeout()))
                .writeTimeout(Duration.ofMillis(commonConfig.getWriteTimeout()))
                .build();
    }

    // Apache HttpClient configuration
    @Bean
    @ConditionalOnProperty(name = "http.client.useOkHttp", havingValue = "false")
    public CloseableHttpClient apacheHttpClient() {
        // Create a connection manager for the Apache HttpClient
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(commonConfig.getMaxTotalConnections()); // Set the maximum number of total connections
        connectionManager.setDefaultMaxPerRoute(16); // Set the maximum number of connections per route (2^4)
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(commonConfig.getConnectTimeout()))
                .setSocketTimeout(Timeout.ofMilliseconds(commonConfig.getReadTimeout()))
                .setTimeToLive(Timeout.ofMilliseconds(commonConfig.getKeepAliveDuration()))
                .setValidateAfterInactivity(Timeout.ofSeconds(5)) // Validate connections after 5 seconds of inactivity
                .build());

        return HttpClients.custom()
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(commonConfig.getRetry_times(), TimeValue.ofMilliseconds(1000))) // Set retry strategy, retry_times with 1000ms interval
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionKeepAlive(TimeValue.ofMilliseconds(commonConfig.getKeepAliveDuration()))
                        .setRedirectsEnabled(true)
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(1000)) // Set the timeout to obtain a connection from the pool
                        .setResponseTimeout(commonConfig.getReadTimeout(), TimeUnit.MILLISECONDS) // Set the maximum response time from the server
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
        if (commonConfig.isUseOkHttp()) {
            restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
        } else {
            restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(apacheHttpClient));
        }
        // Set global interceptors for RestTemplate
        restTemplate.setInterceptors(Collections.singletonList(globalRequestInterceptor));
        return restTemplate;
    }

    // Feign client configuration


    // Global Request Interceptor for Feign and RestTemplate
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RestTemplate.class)
    public ClientHttpRequestInterceptor globalRequestInterceptor() {
        return (request, body, execution) -> {
            // Handle and add additional headers to the request
            RequestHeadersHandler.handleHeaders(commonConfig.getAdditionalHeaders());
            if (!commonConfig.getAdditionalHeaders().isEmpty()) {
                commonConfig.getAdditionalHeaders().keySet().forEach(it -> {
                    request.getHeaders().add(it, commonConfig.getAdditionalHeaders().get(it));
                });
            }
            // Add additional authentication or other information here
            return execution.execute(request, body);
        };
    }
}
