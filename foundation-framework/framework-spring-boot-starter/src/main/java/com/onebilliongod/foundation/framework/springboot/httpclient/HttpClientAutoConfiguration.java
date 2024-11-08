package com.onebilliongod.foundation.framework.springboot.httpclient;


import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "http.client")
public class HttpClientAutoConfiguration {
    // Configuration properties
    // Whether to use OkHttp client
    private boolean useOkHttp = true;

    // The maximum wait time for the client to establish a connection to the server. If the connection cannot be established within the specified time, a timeout exception will be thrown.
    private int connectTimeout = 500;

    // The maximum wait time to read data from the server. If no response is received within this time, a timeout exception will be thrown.
    private int readTimeout = 2000;

    // The maximum wait time to write request data to the server. If the data is not written within this time, a timeout exception will be thrown.
    private int writeTimeout = 1000;

    // The time that a connection will be kept idle in the connection pool. If the connection is not reused within this time, it will be closed.
    private long keepAliveDuration = 10000;

    // The maximum number of idle connections allowed in the connection pool.
    private int maxIdleConnections = 10;

    // The maximum number of connections allowed in the Apache HttpClient connection pool.
    private int maxTotalConnections = 200;

    // Number of retry attempts for requests
    private int retry_times = 3;

    // Additional headers to be included in requests
    private Map<String, String> additionalHeaders;

    // OkHttp Client configuration
    @Bean
    @ConditionalOnProperty(name = "http.client.useOkHttp", havingValue = "true", matchIfMissing = true)
    public OkHttpClient okHttpClient() {
        // Create a connection pool with specified max idle connections and keep-alive duration
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
        // Create a connection manager for the Apache HttpClient
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections); // Set the maximum number of total connections
        connectionManager.setDefaultMaxPerRoute(16); // Set the maximum number of connections per route (2^4)
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
                .setTimeToLive(Timeout.ofMilliseconds(keepAliveDuration))
                .setValidateAfterInactivity(Timeout.ofSeconds(5)) // Validate connections after 5 seconds of inactivity
                .build());

        return HttpClients.custom()
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(retry_times, TimeValue.ofMilliseconds(1000))) // Set retry strategy, retry_times with 1000ms interval
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionKeepAlive(TimeValue.ofMilliseconds(keepAliveDuration))
                        .setRedirectsEnabled(true)
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(1000)) // Set the timeout to obtain a connection from the pool
                        .setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS) // Set the maximum response time from the server
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
        // Set global interceptors for RestTemplate
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
            // Handle and add additional headers to the request
            RequestHeadersHandler.handleHeaders(additionalHeaders);
            if (!additionalHeaders.isEmpty()) {
                additionalHeaders.keySet().forEach(it -> {
                    request.getHeaders().add(it, additionalHeaders.get(it));
                });
            }
            // Add additional authentication or other information here
            return execution.execute(request, body);
        };
    }

    // Request interceptor for Feign client
    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            // Handle and set headers for Feign requests
            RequestHeadersHandler.handleHeaders(additionalHeaders);
            if (!additionalHeaders.isEmpty()) {
                additionalHeaders.keySet().forEach(it -> {
                    template.header(it, additionalHeaders.get(it));
                });
            }
        };
    }
}
