package com.onebilliongod.foundation.framework.springboot.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The main purpose of this class is to scan custom annotations on Bean definitions, parse these annotations, extract configuration information from them,
 * and add this configuration information as property sources to the Spring Environment, thereby influencing the configuration and behavior of the entire Spring application.
 * @param <A>
 */
public abstract class AnnotationPropertySourcePostProcessor<A extends Annotation> implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware {
    protected Environment environment;

    protected ResourceLoader resourceLoader;

    protected ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return;
        }

        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            BeanDefinition obd = bd.getOriginatingBeanDefinition();
            if (obd != null) {
                bd = obd;
            }
            if (!((bd) instanceof GenericBeanDefinition)) {
                continue;
            }

            Collection<A> annotations = annotations((GenericBeanDefinition)bd);
            if (annotations.isEmpty()) {
                continue;
            }

            annotations.forEach(annotation -> {
                List<PropertySource<?>> propertySources = processPropertySource(annotation);
                propertySources.forEach(this::addPropertySource);
            });
        }


    }

    @NonNull
    public abstract List<PropertySource<?>> processPropertySource(A annotation) throws BeansException;

    private Set<A> annotations(GenericBeanDefinition beanDefinition) {
        Class<?> beanClass = beanClass(beanDefinition);
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(beanClass, annotationType());
    }

    public abstract Class<A> annotationType();

    private Class<?> beanClass(GenericBeanDefinition beanDefinition) throws BeansException{
        if (!beanDefinition.hasBeanClass()) {
            try {
                beanDefinition.resolveBeanClass(beanClassLoader);
            } catch (ClassNotFoundException e) {
                throw new BeanDefinitionValidationException(e.getMessage());
            }
        }
        return beanDefinition.getBeanClass();
    }

    protected void addPropertySource(PropertySource<?> propertySource) {
        String name = propertySource.getName();
        MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment).getPropertySources();
        List<String> sourceNames = sourceNames();

        if (!sourceNames.contains(name)) {
            if (sourceNames.isEmpty()) {
                propertySources.addLast(propertySource);
            } else {
                String processed = sourceNames.get(sourceNames.size() - 1);
                propertySources.addBefore(processed, propertySource);
            }
            sourceNames.add(name);
            return;
        }

        PropertySource<?> current = propertySources.get(name);
        if (current == null) {
            return;
        }

        PropertySource<?> wrapped = (propertySource instanceof ResourcePropertySource ?
                ((ResourcePropertySource) propertySource).withResourceName() : propertySource);

        if (current instanceof CompositePropertySource) {
            ((CompositePropertySource) current).addFirstPropertySource(wrapped);
            return;
        }

        if (current instanceof ResourcePropertySource) {
            current = ((ResourcePropertySource) current).withResourceName();
        }

        CompositePropertySource composite = new CompositePropertySource(name);
        composite.addPropertySource(wrapped);
        composite.addPropertySource(current);
        propertySources.replace(name, composite);
    }

    public abstract List<String> sourceNames();
}
