package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.influx.InfluxConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * management:
 *   metrics:
 *     export:
 *       influx:
 *         uri: http://my-influxdb:8086
 *         db: custom-metrics
 *         userName: custom-user
 *         password: custom-password
 *         step: 10s
 */
@ConfigurationProperties(prefix = "management.metrics.export.influx")
public class InfluxProperties implements InfluxConfig {
    private String uri;
    private String db;

    private String userName;
    private String password;

    private String step;

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDb(String db) {
        this.db = db;
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

    @Override
    public String get(String key) {
        if (key.equals(this.prefix() + ".uri")) {
            return uri;
        } else if (key.equals(this.prefix() + ".userName")) {
            return userName;
        } else if (key.equals(this.prefix() + ".password")) {
            return password;
        } else if (key.equals(this.prefix() + ".step")) {
            return step;
        } else if (key.equals(this.prefix() + ".db")) {
            return db;
        }
        return null;
    }
}
