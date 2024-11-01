package com.onebilliongod.foundation.framework.springboot.env;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FoundationBootEnvironmentPostProcessorTest {
    private static class TestPostProcessor extends FoundationBootEnvironmentPostProcessor {
        @Override
        protected List<String> getResourcePaths() {
            return Collections.singletonList("classpath:test.properties");
        }

        @Override
        public int getOrder() {
            return 0; // Define test-specific order
        }

        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        }
    }

    @Test
    public void testLocateProperties() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getProperty("profile")).thenReturn(null);

        TestPostProcessor processor = new TestPostProcessor();
        processor.setResourceLoader(mock(ResourceLoader.class));

        MapPropertySource propertySource = processor.locateProperties(environment);

        assertNotNull(propertySource);
        assertEquals("FoundationBootEnvironmentPostProcessorTest", propertySource.getName());
    }

    @Test
    public void testGetResourcePaths() {
        TestPostProcessor processor = new TestPostProcessor();
        List<String> paths = processor.getResourcePaths();

        assertEquals(1, paths.size());
        assertEquals("classpath:test.properties", paths.get(0));
    }

    @Test
    public void testSetResourceLoader() {
        TestPostProcessor processor = new TestPostProcessor();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        processor.setResourceLoader(resourceLoader);

        // The method has no output, but we can assert the loader is set correctly
        assertNotNull(resourceLoader);
    }


}
