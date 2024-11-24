package com.onebilliongod.foundation.framework.springboot.monitoring;

import com.onebilliongod.foundation.framework.springboot.httpclient.FeignAutoConfiguration;
import com.onebilliongod.foundation.framework.springboot.httpclient.HttpClientAutoConfiguration;
import com.onebilliongod.foundation.framework.springboot.httpclient.RestTemplateAutoConfiguration;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MetricsExportAutoTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MetricsExportConfig.class,
                    MetricsExportAutoConfiguration.class));

    @Test
    public void testPrometheusMeterRegistry() {
        this.contextRunner.withPropertyValues("").run((context) -> {
            assertThat(context).hasSingleBean(PrometheusMeterRegistry.class);
        });
    }

}
