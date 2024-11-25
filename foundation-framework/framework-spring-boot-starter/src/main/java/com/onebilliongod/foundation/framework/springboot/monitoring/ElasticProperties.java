package com.onebilliongod.foundation.framework.springboot.monitoring;

import io.micrometer.elastic.ElasticConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * management:
 *   metrics:
 *     export:
 *       elastic:
 *         host: http://my-elastic:9200
 *         index: custom-metrics
 *         step: 5s
 */
@ConfigurationProperties(prefix = "management.metrics.export.elastic")
public class ElasticProperties implements ElasticConfig {
    private String host;
    private String index = "foundation-metrics";
    private String userName;
    private String password;
    private String step;

    private String apiKeyCredentials;

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

    public void setApiKeyCredentials(String apiKeyCredentials) {
        this.apiKeyCredentials = apiKeyCredentials;
    }

    @Override
    public String get(String key) {
        if (key.equals(this.prefix() + ".host")) {
            return host;
        } else if (key.equals(this.prefix() + ".index")) {
            return index;
        } else if (key.equals(this.prefix() + ".userName")) {
            return userName;
        } else if (key.equals(this.prefix() + ".password")) {
            return password;
        } else if (key.equals(this.prefix() + ".step")) {
            return step;
        } else if (key.equals(this.prefix() + ".apiKeyCredentials")) {
            return apiKeyCredentials;
        }
        return null;
    }
}
