package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.elastic.ElasticConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.micrometer.core.instrument.config.validate.PropertyValidator.getString;

@ConfigurationProperties(prefix = "management.metrics.export.elastic")
public class ElasticProperties implements ElasticConfig {
    private String host;
    private String index = "foundation-metrics";
    private String userName;
    private String password;
    private String step;

    @NotNull
    @Override
    public String host() {
        return host;
    }

    @Override
    @NotNull
    public String index() {
        return index;
    }

    @Override
    public String userName() {
        return userName;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String get(String key) {
        return null;
    }
}
