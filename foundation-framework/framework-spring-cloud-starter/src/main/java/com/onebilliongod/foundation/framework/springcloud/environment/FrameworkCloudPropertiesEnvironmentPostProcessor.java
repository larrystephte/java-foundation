package com.onebilliongod.foundation.framework.springcloud.environment;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class FrameworkCloudPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource("META-INF/spring.otel.properties");
        if (resource.exists()) {
            registerOtelPropertiesPropertySource(environment, resource);
        }
    }

    protected void registerOtelPropertiesPropertySource(ConfigurableEnvironment environment, Resource resource) {
        PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();
        try {
            OriginTrackedMapPropertySource propertyFileSource = (OriginTrackedMapPropertySource) loader
                    .load("META-INF/spring.otel.properties", resource)
                    .get(0);
            environment.getPropertySources().addLast(propertyFileSource);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to load otel properties from " + resource, ex);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
