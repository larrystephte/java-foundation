package com.onebilliongod.foundation.framework.springboot.httpclient;

import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@ConditionalOnClass(Feign.class)
public class FeignConfiguration {
    @Bean
    @ConditionalOnMissingBean
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
