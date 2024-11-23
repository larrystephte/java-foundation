package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.elastic.ElasticConfig;
import io.micrometer.influx.InfluxConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * management:
 *   metrics:
 *     export:
 *       elastic:
 *         host: http://my-elastic:9200
 *         index: custom-metrics
 *         step: 5s
 *       influx:
 *         uri: http://my-influxdb:8086
 *         db: custom-metrics
 *         user: custom-user
 *         password: custom-password
 *         step: 10s
 */
@Configuration
public class MetricsExportConfig {
    @Bean
    @ConfigurationProperties("management.metrics.export.elastic")
    public ElasticConfig elasticConfig() {
        return key -> null;
    }

    @Bean
    @ConfigurationProperties("management.metrics.export.influx")
    public InfluxConfig influxConfig() {
        return key -> null;
    }
}
