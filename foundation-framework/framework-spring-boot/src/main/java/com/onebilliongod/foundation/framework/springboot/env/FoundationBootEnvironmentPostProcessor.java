package com.onebilliongod.foundation.framework.springboot.env;

import com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool;
import com.onebilliongod.foundation.framework.springboot.utils.SelectorLocalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePatternUtils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.*;

/**
 * Abstract base class for processing environment properties in a Spring Boot application.
 * <p>
 * This class implements the EnvironmentPostProcessor interface to allow custom property
 * sources to be added to the application's environment during startup.
 *
 * Provides default configurations for some common components (such as Apollo, Consul, etc.), simplifying user configuration.
 * This EnvironmentPostProcessor has the lowest execution priority, ensuring that users can override these configurations.
 * </p>
 */
@Slf4j
public abstract class FoundationBootEnvironmentPostProcessor implements EnvironmentPostProcessor, ResourceLoaderAware, Ordered {
    private ResourceLoader resourceLoader;

    /**
     * Returns the name of the property source based on the class name.
     */
    protected String getPropertySourceName() {
        return this.getClass().getName();
    }

    /**
     * Locates properties based on the current environment's profile and cloud configuration.
     *
     */
    protected MapPropertySource locateProperties(Environment environment) {
        Map<String, Object> container = new HashMap<>();
        String profile = ContextEnvironmentTool.profile(environment);
        String cloud = ContextEnvironmentTool.cloud(environment);
        try {
            Collection<EncodedResource> resources = getResourcePatternResolver(getResourcePaths());
            for (EncodedResource item : resources) {
                Properties properties = PropertiesLoaderUtils.loadProperties(item);
                log.info("Loading profile：" + item.getResource().getFilename());
                container.putAll(SelectorLocalizer.localize(properties, profile, cloud));
            }
        } catch (IOException e) {
            log.error("Failed to load framework boot configuration files, the system will exit.", e);
            throw new IllegalStateException("Failed to load framework boot configuration files, the system will exit.", e);
        } catch (Exception e) {
            log.error("Unexpected error during property loading.", e);
            throw new IllegalStateException("Unexpected error during property loading.", e);
        }
        return new MapPropertySource(getPropertySourceName(), container);
    }

    /**
     * Resolves resource paths to locate configuration files.
     *
     */
    private Collection<EncodedResource> getResourcePatternResolver(List<String> paths) throws IOException {
        Collection<EncodedResource> resources = new LinkedList<>();
        for (String path : paths) {
            Resource[] ss = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(path);
            for (Resource resource : ss) {
                resources.add(new EncodedResource(resource, ContextEnvironmentTool.DEFAULT_CHARSET));
            }
        }
        return resources;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Returns the list of resource paths that need to be resolved.
     */
    protected abstract List<String> getResourcePaths();

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Adjust this value to set the order of execution
    }
}
