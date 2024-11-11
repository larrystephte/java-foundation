package com.onebilliongod.foundation.framework.springboot.httpclient;

import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

@AutoConfiguration
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties({HttpClientProperties.class})
public class FeignAutoConfiguration {
    private final HttpClientProperties commonConfig;

    public FeignAutoConfiguration(HttpClientProperties commonConfig) {
        this.commonConfig = commonConfig;
    }

    // Feign client configuration
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder(@Nullable OkHttpClient okHttpClient,
                                      @Nullable CloseableHttpClient apacheHttpClient,
                                      RequestInterceptor feignRequestInterceptor) {
        if (commonConfig.isUseOkHttp()) {
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
            RequestHeadersHandler.handleHeaders(commonConfig.getAdditionalHeaders());
            if (!commonConfig.getAdditionalHeaders().isEmpty()) {
                commonConfig.getAdditionalHeaders().keySet().forEach(it -> {
                    template.header(it, commonConfig.getAdditionalHeaders().get(it));
                });
            }
        };
    }
}
