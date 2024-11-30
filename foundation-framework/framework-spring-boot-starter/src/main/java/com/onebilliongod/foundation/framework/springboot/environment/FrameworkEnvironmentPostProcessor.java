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

        Map<String, Object> predefinedVariables  = new HashMap<>();

        //check the  applicationName
        String applicationName = resolver.getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            throw new IllegalArgumentException("Please configure spring.application.name");
        }


        return predefinedVariables;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
