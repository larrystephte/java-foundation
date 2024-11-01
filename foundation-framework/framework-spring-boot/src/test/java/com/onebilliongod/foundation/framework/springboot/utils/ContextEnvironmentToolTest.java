package com.onebilliongod.foundation.framework.springboot.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import com.onebilliongod.foundation.commons.core.net.NetworkUtils;

public class ContextEnvironmentToolTest {
    @Test
    public void testProfileWithDirectProperty() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty("profile")).thenReturn("test-dev");

        String profile = ContextEnvironmentTool.profile(environment);
        assertEquals("test", profile);
    }

    @Test
    public void testProfileWithActiveProfiles() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        String profile = ContextEnvironmentTool.profile(environment);
        assertEquals("dev", profile);
    }

    @Test
    public void testCloudWithDirectProperty() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty("cloud")).thenReturn("azure");

        String cloud = ContextEnvironmentTool.cloud(environment);
        assertEquals("azure", cloud);
    }

    @Test
    public void testCloudInferredFromHostName() {
        // Mock NetworkUtils to return a specific hostname
        try (MockedStatic<NetworkUtils> mockedStatic = mockStatic(NetworkUtils.class)) {
            String mockHostName = "myapp.aws.example.com";
            mockedStatic.when(NetworkUtils::getHostName).thenReturn(mockHostName);

            Environment environment = Mockito.mock(Environment.class);
            String cloud = ContextEnvironmentTool.cloud(environment);
            assertEquals("aws", cloud);
        }
    }
}
