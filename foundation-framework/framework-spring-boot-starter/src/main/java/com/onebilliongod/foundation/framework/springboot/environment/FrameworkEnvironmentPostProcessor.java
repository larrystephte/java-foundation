package com.onebilliongod.foundation.framework.springboot.environment;

import com.onebilliongod.foundation.commons.core.common.Constants;
import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool;
import com.onebilliongod.foundation.framework.springboot.utils.K8SUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *  FrameworkEnvironmentPostProcessor is used to customize the Spring application environment
 *  during the startup phase. It implements the EnvironmentPostProcessor and Ordered interfaces,
 *  allowing it to modify environment properties and control the order in which it is applied.
 */
@Slf4j
public class FrameworkEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final int ORDER = HIGHEST_PRECEDENCE + 100;

    /**
     * Post-processes the Spring application environment by adding custom property sources.
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> predefinedProperties = predefinedProperties(environment);
        propertySources.addLast(new MapPropertySource("predefined-variables", predefinedProperties));

        Map<String, Object> defaultProperties = loadDefaultProperties();
        propertySources.addLast(new MapPropertySource("framework-defaults", defaultProperties));
    }

    private Map<String, Object> loadDefaultProperties() {
        Map<String, Object> defaultProperties = new HashMap<>();
        Resource resource = new ClassPathResource("META-INF/spring-boot-defaults/application.yml");
        if (resource.exists()) {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            try {
                PropertySource<?> yamlProperties = loader.load("framework-defaults", resource).get(0);
                defaultProperties.putAll(((MapPropertySource) yamlProperties).getSource());
            } catch (IOException e) {
                log.error("fail loading defaults application.yml,the error is " + e.getMessage());
            }
        }
        return defaultProperties;
    }

    private Map<String, Object> predefinedProperties(final ConfigurableEnvironment environment) {
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(environment.getPropertySources());

        //resolve localIp
        Map<String, Object> predefinedVariables  = new HashMap<>();
        String localIp = NetworkUtils.getLocalIp();
        if (localIp == null) {
            throw new IllegalStateException("The framework could not resolve the local IP address (IPv4). Please review the hosts configuration!");
        }
        predefinedVariables.put("localIp", localIp);

        //resolve hostName
        String hostName = NetworkUtils.getHostName();
        if (hostName == null) {
            throw new IllegalStateException("The framework could not resolve the local hostname. Please review the hosts configuration!");
        }
        predefinedVariables.put("hostName", hostName);
        System.setProperty("hostName", hostName);
        String k8sHostName = resolver.getProperty("HOSTNAME");
        if (StringUtils.isBlank(k8sHostName)) {
            k8sHostName = hostName;
        }
        System.setProperty("HOSTNAME", k8sHostName);
        predefinedVariables.put("HOSTNAME", k8sHostName);

        //resolve profile
        String profile = ContextEnvironmentTool.profile(environment);
        predefinedVariables.put(Constants.PROFILE, profile);
        System.setProperty(Constants.PROFILE, profile);

        //resolve group
        String group = ContextEnvironmentTool.group(environment);
        predefinedVariables.put(Constants.GROUP, group);
        System.setProperty(Constants.GROUP, group);

        //check the  applicationName
        String applicationName = resolver.getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            throw new IllegalArgumentException("Please configure spring.application.name");
        }
        predefinedVariables.put(Constants.PROJECT, applicationName);
        System.setProperty(Constants.PROJECT, applicationName);

        //check the cloud
        String cloud = ContextEnvironmentTool.cloud(environment);
        System.setProperty(Constants.CLOUD, cloud);
        predefinedVariables.put(Constants.CLOUD, applicationName);

        //resolve the  k8sNamespace
        String k8sNamespace = resolver.getProperty("K8S_NAMESPACE");
        if (StringUtils.isBlank(k8sNamespace)) {
            k8sNamespace = K8SUtil.podOfNamespace("default");
        }
        System.setProperty("K8S_NAMESPACE", k8sNamespace);
        predefinedVariables.put("K8S_NAMESPACE", k8sNamespace);

        return predefinedVariables;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
