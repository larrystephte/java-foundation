package com.onebilliongod.foundation.framework.springboot.monitoring;

import com.onebilliongod.foundation.framework.springboot.httpclient.FeignAutoConfiguration;
import com.onebilliongod.foundation.framework.springboot.httpclient.HttpClientAutoConfiguration;
import com.onebilliongod.foundation.framework.springboot.httpclient.RestTemplateAutoConfiguration;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import io.micrometer.influx.InfluxMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MetricsExportAutoTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(
//                    ElasticMeterRegistry.class,
                    InfluxMeterRegistry.class,
                    PrometheusMeterRegistry.class
            ))
            .withConfiguration(AutoConfigurations.of(MetricsExportConfig.class,
                    MetricsExportAutoConfiguration.class,
                    ConfigurationPropertiesAutoConfiguration.class
              ))
//            .withBean(PrometheusMeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT))
            .withPropertyValues(
                    "management.metrics.export.enabled=true",
                    "management.metrics.export.prometheus.enabled=true"
            );

    @Test
    public void testPrometheusMeterRegistry() {
        this.contextRunner.withPropertyValues("").run((context) -> {
            assertThat(context).hasSingleBean(PrometheusMeterRegistry.class);
        });
    }

    @Test
    public void testElasticMeterRegistry() {
        //TODO test some config
        this.contextRunner.withPropertyValues("management.metrics.export.elastic.host=http://my-elastic:9200").run((context) -> {
            assertThat(context).hasSingleBean(ElasticMeterRegistry.class);

            ElasticConfig e = context.getBean(ElasticConfig.class);
            assertThat(e.host()).isEqualTo("http://my-elastic:9200");
        });
    }

}
