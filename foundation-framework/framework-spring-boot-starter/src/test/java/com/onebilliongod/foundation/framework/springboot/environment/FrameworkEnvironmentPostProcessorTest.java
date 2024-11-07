package com.onebilliongod.foundation.framework.springboot.environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class FrameworkEnvironmentPostProcessorTest {
    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private SpringApplication application;

    private FrameworkEnvironmentPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postProcessor = new FrameworkEnvironmentPostProcessor();
    }

    @Test
    void testPostProcessEnvironment() {
        // Arrange: Set up mocks and expectations
        MutablePropertySources propertySources = new MutablePropertySources();

//        MockPropertySource predefinedVariablesSource = new MockPropertySource("predefined-variables");
//        MockPropertySource frameworkDefaultsSource = new MockPropertySource("framework-defaults");

        // Mock environment methods
        when(environment.getPropertySources()).thenReturn(propertySources);
        when(environment.getProperty("spring.application.name")).thenReturn("testApp");
        when(environment.getProperty("HOSTNAME")).thenReturn("testHost");
        when(environment.getProperty("K8S_NAMESPACE")).thenReturn("test_namespace");
        when(environment.getProperty("profile")).thenReturn("test-group1");

//        Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        // Act: Execute the method to be tested
        postProcessor.postProcessEnvironment(environment, application);

        PropertySource<?> predefinedVariables = propertySources.get("predefined-variables");
        assertNotNull(predefinedVariables, "Predefined variables property source should be added");
        assertEquals("test", predefinedVariables.getProperty("profile"));
        assertEquals("group1", predefinedVariables.getProperty("group"));
        assertEquals("testHost", predefinedVariables.getProperty("HOSTNAME"));
        assertEquals("test_namespace", predefinedVariables.getProperty("K8S_NAMESPACE"));
    }
}
