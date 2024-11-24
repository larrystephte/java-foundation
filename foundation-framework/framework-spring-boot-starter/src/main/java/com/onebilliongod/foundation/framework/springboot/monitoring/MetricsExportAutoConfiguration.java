package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(MetricsExportConfig.class)
@ConditionalOnProperty(name = "management.metrics.export.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsExportAutoConfiguration {
    @Bean
    @ConditionalOnClass(name = "io.micrometer.prometheus.PrometheusMeterRegistry")
    public PrometheusMeterRegistry prometheusMeterRegistry(PrometheusConfig config) {
        return new PrometheusMeterRegistry(config);
    }

    @Bean
    @ConditionalOnClass(name = "io.micrometer.elastic.ElasticMeterRegistry")
    public ElasticMeterRegistry elasticMeterRegistry(ElasticConfig config) {
        return new ElasticMeterRegistry(config, Clock.SYSTEM);
    }

    @Bean
    @ConditionalOnClass(name = "io.micrometer.influx.InfluxMeterRegistry")
    public InfluxMeterRegistry influxMeterRegistry(InfluxConfig config) {
        return new InfluxMeterRegistry(config, Clock.SYSTEM);
    }

}
