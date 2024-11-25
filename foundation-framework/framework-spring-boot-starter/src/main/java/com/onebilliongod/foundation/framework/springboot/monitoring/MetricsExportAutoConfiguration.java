package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.elastic.ElasticMeterRegistry;
import io.micrometer.influx.InfluxMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({ElasticProperties.class, PrometheusProperties.class, InfluxProperties.class})
@ConditionalOnProperty(name = "management.metrics.export.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsExportAutoConfiguration {
    @Bean
    @ConditionalOnClass(PrometheusMeterRegistry.class)
    @ConditionalOnMissingBean(PrometheusMeterRegistry.class)
    public PrometheusMeterRegistry prometheusMeterRegistry(PrometheusProperties config) {
        return new PrometheusMeterRegistry(config);
    }

    @Bean
    @ConditionalOnClass(ElasticMeterRegistry.class)
    @ConditionalOnMissingBean(ElasticMeterRegistry.class)
    public ElasticMeterRegistry elasticMeterRegistry(ElasticProperties config) {
        return new ElasticMeterRegistry(config, Clock.SYSTEM);
    }

    @Bean
    @ConditionalOnClass(InfluxMeterRegistry.class)
    @ConditionalOnMissingBean(InfluxMeterRegistry.class)
    public InfluxMeterRegistry influxMeterRegistry(InfluxProperties config) {
        return new InfluxMeterRegistry(config, Clock.SYSTEM);
    }

}
