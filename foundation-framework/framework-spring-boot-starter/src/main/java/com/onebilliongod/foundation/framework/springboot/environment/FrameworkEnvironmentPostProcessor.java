package com.onebilliongod.foundation.framework.springboot.environment;

import com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool;
import com.onebilliongod.foundation.framework.springboot.utils.K8SUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;

import static com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool.DEFAULT_CHARSET;

/**
 *  FrameworkEnvironmentPostProcessor is used to customize the Spring application environment
 *  during the startup phase. It implements the EnvironmentPostProcessor and Ordered interfaces,
 *  allowing it to modify environment properties and control the order in which it is applied.
 */
public class FrameworkEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final int ORDER = HIGHEST_PRECEDENCE + 100;

    public static final String SOURCE_NAME_FIRST = "framework-boot-env-first";

    public static final String SOURCE_NAME_LAST = "framework-boot-env-last";

    private final Map<String, Object> lastSource = new HashMap<>();

    private final Map<String, Object> firstSource = new HashMap<>();

    public static final String SERVICE_LOG_IN_K8S_PATH = "/data/logs/${K8S_POD_NAMESPACE:default_namespace}/${spring.application.name}/${HOSTNAME}";


    /**
     * Post-processes the Spring application environment by adding custom property sources.
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();

        if (propertySources.contains(SOURCE_NAME_FIRST) || propertySources.contains(SOURCE_NAME_LAST)) {
            return;
        }

        MapPropertySource lastPropertySource = new MapPropertySource(SOURCE_NAME_LAST, lastSource);
        propertySources.addLast(lastPropertySource);

        MapPropertySource firstPropertySource = new MapPropertySource(SOURCE_NAME_FIRST, firstSource);
        propertySources.addFirst(firstPropertySource);

        //resolve logging environment varies
        resolveLogging(environment);
    }

    /**
     * Resolves the logging configuration based on the environment.
     * The method sets up various Logging properties.
     * @param environment
     */
    private void resolveLogging(ConfigurableEnvironment environment) {
        // Set logback configuration files
        lastSourcePut(environment, "logging.config", "classpath:com/onebilliongod/foundation/framework/springboot/logback-meteor.xml");
        lastSourcePut(environment, "logback.access.config", "classpath:com/onebilliongod/foundation/framework/springboot/logback-meteor-access.xml");

        //Set log file path based on the environment
        lastSourcePut(environment, "logging.file.path", buildLoggingPath(environment));

        //Set other logging properties such as log levels and log rolling policies
        lastSourcePut(environment, "logging.group.foundation",  "com.onebilliongod.foundation.framework.springboot,com.onebilliongod.foundation.framework.springcloud");
        lastSourcePut(environment, "logging.level.foundation", "INFO");
        lastSourcePut(environment, "logging.level.org.springframework.context.support", "WARN");
        lastSourcePut(environment, "logging.logback.rollingpolicy.max-history", "3");
        lastSourcePut(environment, "logging.logback.rollingpolicy.max-file-size", "128MB");
        lastSourcePut(environment, "logging.logback.rollingpolicy.total-size-cap", "12GB");
        lastSourcePut(environment, "logging.charset.file", DEFAULT_CHARSET);//官方default为UTF8
        lastSourcePut(environment, "logging.charset.console", DEFAULT_CHARSET);//官方default为UTF8
    }

    /**
     * Adds a property to the `lastSource` map if the property does not already exist in the environment.
     *  This method ensures that the user's custom configuration (e.g., in `bootstrap.properties`) is not overwritten.
     * @param environment
     * @param key
     * @param value
     */
    void lastSourcePut(ConfigurableEnvironment environment, String key, Object value) {
        // Only add the property if it doesn't already exist in the environment
        if (environment.getProperty(key) == null) {
            lastSource.put(key, value);
        }
    }


    /**
     * Builds the log path for the service, differentiating between Kubernetes and VM environments.
     * @param environment
     * @return
     */
    private String buildLoggingPath(Environment environment) {
        //Check if the service is running inside Kubernetes
        if (K8SUtil.serviceInK8s(environment)) {
            final String namespace = K8SUtil.podOfNamespace("default");
            return SERVICE_LOG_IN_K8S_PATH.replace("default_namespace", namespace);
        } else {
            // In a VM environment, construct the log path using profile and group information
            final String profile = ContextEnvironmentTool.profile(environment);
            final String group = ContextEnvironmentTool.group(environment);
            return "/data/logs/${spring.application.name}-" + profile + "-" + group;
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
