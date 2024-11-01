package com.onebilliongod.foundation.framework.springboot.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class YamlSourcePostProcessor extends AnnotationPropertySourcePostProcessor<YamlSource> {
    private final PropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();

    private final List<String> yamlSourceNames = new ArrayList<>();

    @Override
    public List<PropertySource<?>> processPropertySource(YamlSource annotation) throws BeansException {
        String[] locations = annotation.value();
        Assert.isTrue(locations.length > 0, "It must contain a file path as the value of the @YamlSource annotation");
        boolean ignoreResourceNotFound = annotation.ignoreResourceNotFound();

        List<PropertySource<?>> propertySources = new ArrayList<>();
        for (String location : locations) {
            try {
                Resource resource = this.resourceLoader.getResource(resolveLocation(location));
                String resourceName = getResourceName(annotation.name(),resource);
                propertySources.addAll(yamlPropertySourceLoader.load(resourceName, resource));
            } catch (IllegalArgumentException | FileNotFoundException | UnknownHostException ex) {
                if (ignoreResourceNotFound) {
                    if (logger.isInfoEnabled()) {
                        logger.info("@YamlSource file path [" + location + "] cant be parse，" + ex.getMessage());
                    }
                } else {
                    throw new BeanInitializationException(ex.getMessage());
                }
            } catch (IOException ie) {
                throw new BeanInitializationException(ie.getMessage());
            }
        }
        return propertySources;
    }

    @Override
    public List<String> sourceNames() {
        return yamlSourceNames;
    }

    @Override
    public Class<YamlSource> annotationType() {
        return YamlSource.class;
    }
}
