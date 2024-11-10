package com.onebilliongod.foundation.framework.springboot.httpclient;

import feign.Feign;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties({HttpClientProperties.class})
public class RestTemplateAutoConfiguration {
    private final HttpClientProperties commonConfig;

    public RestTemplateAutoConfiguration(HttpClientProperties commonConfig) {
        this.commonConfig = commonConfig;
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
