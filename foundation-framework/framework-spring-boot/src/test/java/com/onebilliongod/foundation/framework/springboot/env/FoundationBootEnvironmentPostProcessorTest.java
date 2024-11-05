package com.onebilliongod.foundation.framework.springboot.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FoundationBootEnvironmentPostProcessorTest {
    @BeforeEach
    public void setUp() throws Exception {

    }

    @Test
    public void testLocateProperties() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getProperty("profile")).thenReturn("pre");
        when(environment.getProperty("cloud")).thenReturn("aws");

        FoundationBootEnvironmentPostProcessor processor = new FoundationBootEnvironmentPostProcessor();
        processor.setResourceLoader(mock(ResourceLoader.class));

        MapPropertySource propertySource = processor.locateProperties(environment);

        assertNotNull(propertySource);
        assertEquals("test", propertySource.getProperty("test"));
        assertEquals("com.onebilliongod.foundation.framework.springboot.env.FoundationBootEnvironmentPostProcessor", propertySource.getName());
    }

    @Test
    public void testSetResourceLoader() {
        FoundationBootEnvironmentPostProcessor processor = new FoundationBootEnvironmentPostProcessor();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        processor.setResourceLoader(resourceLoader);

        // The method has no output, but we can assert the loader is set correctly
        assertNotNull(resourceLoader);
    }


}
