package com.onebilliongod.foundation.framework.springboot.utils;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class SelectorLocalizerTest {
    @Test
    void testLocalizeWithNonSelectorProperties() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0");

        Map<String, Object> result = SelectorLocalizer.localize(properties, "dev", "cloud");
        assertEquals("TestApp", result.get("app.name"));
        assertEquals("1.0", result.get("app.version"));
    }

    @Test
    void testLocalizeWithSelectorProperties() {
        Properties properties = new Properties();
        properties.setProperty("_selector_.dev.aws.someProperty", "Value1");
        properties.setProperty("_selector_.dev.azure.someProperty", "Value2");
        properties.setProperty("_selector_.default.aws.someProperty", "DefaultValue");

        Map<String, Object> result = SelectorLocalizer.localize(properties, "dev", "aws");

        assertEquals("Value1", result.get("someProperty"));
    }

    @Test
    void testLocalizeWithDefaultCloud() {
        Properties properties = new Properties();
        properties.setProperty("_selector_.dev.default.someProperty", "DefaultValue");

        Map<String, Object> result = SelectorLocalizer.localize(properties, "dev", "default");

        assertEquals("DefaultValue", result.get("someProperty"));
    }

    @Test
    void testLocalizeWithNullProperties() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SelectorLocalizer.localize(null, "dev", "default");
        });

        assertEquals("Properties cannot be null", exception.getMessage());
    }
}
