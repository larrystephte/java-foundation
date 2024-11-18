package com.onebilliongod.foundation.framework.springboot.httpclient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@AutoConfiguration
@EnableAutoConfiguration
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties({HttpClientProperties.class})
@Slf4j
public class RestTemplateAutoConfiguration {
    private final HttpClientProperties commonConfig;

    public RestTemplateAutoConfiguration(HttpClientProperties commonConfig) {
        this.commonConfig = commonConfig;
    }
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultRestTemplateCustomizer restTemplateCustomizer(@Nullable OkHttpClient okHttpClient,
                                                                @Nullable CloseableHttpClient apacheHttpClient) {
        ClientHttpRequestFactory clientHttpRequestFactory;
        if (commonConfig.isUseOkHttp()) {
            clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        } else {
            clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(apacheHttpClient);
        }
        return new DefaultRestTemplateCustomizer(clientHttpRequestFactory);
    }

    public class DefaultRestTemplateCustomizer implements RestTemplateCustomizer {

        private final ClientHttpRequestFactory clientHttpRequestFactory;

        public DefaultRestTemplateCustomizer(ClientHttpRequestFactory clientHttpRequestFactory) {
            this.clientHttpRequestFactory = clientHttpRequestFactory;
        }

        @Override
        public void customize(RestTemplate restTemplate) {
            restTemplate.setRequestFactory(clientHttpRequestFactory);

            restTemplate.getInterceptors().add(new RequestHeadersHandler(commonConfig.getAdditionalHeaders()));

            restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        }
    }

    static class CustomResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            System.out.println("Handling error: " + response.getStatusCode());
            super.handleError(response);
        }
    }

}

