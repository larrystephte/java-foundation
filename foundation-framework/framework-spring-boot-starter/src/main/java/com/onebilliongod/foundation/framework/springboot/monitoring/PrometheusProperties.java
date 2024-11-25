package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.metrics.export.prometheus")
public class PrometheusProperties implements PrometheusConfig {
    private String descriptions;
    private String step;

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @Override
    public String get(String key) {
        if (key.equals(this.prefix() + ".descriptions")) {
            return descriptions;
        } else if (key.equals(this.prefix() + ".step")) {
            return step;
        }

        return null;
    }
}
