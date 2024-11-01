package com.onebilliongod.foundation.framework.springboot.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to localize properties based on profile and cloud.
 */
public final class SelectorLocalizer {
    private static final String SELECTOR_PREFIX = "_selector_";
    private static final String DEFAULT_CLOUD = "default";
    private static final Pattern PATTERN = Pattern.compile(SELECTOR_PREFIX + "\\.([a-z]+)\\.([a-z]+)\\.(\\S+)");


    /**
     * Localizes the properties based on the given profile and cloud.
     *
     * @param properties the properties to localize
     * @param profile    the active profile
     * @param cloud      the cloud environment
     * @return a map of localized properties
     * @throws IllegalArgumentException if properties is null
     */
    public static Map<String, Object> localize(Properties properties, String profile, String cloud) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        Map<String, Object> container = new HashMap<>();
        Map<String, Map<String, Map<String, Object>>> selector = new HashMap<>();

        // Process each property
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(SELECTOR_PREFIX)) {
                // Directly add non-selector properties
                container.put(key, properties.getProperty(key));
            } else {
                processSelectorProperty(properties, key, selector);
            }
        }

        container.putAll(getLocalizedSelectorProperties(selector, profile, cloud));
        return container;
    }

    private static void processSelectorProperty(Properties properties, String key,
                                                Map<String, Map<String, Map<String, Object>>> selector) {
        Matcher matcher = PATTERN.matcher(key);

        if (!matcher.find() || matcher.groupCount() != 3) {
            return;
        }

        String property = matcher.group(3);
        if (StringUtils.isEmpty(property)) {
            return;
        }

        Map<String, Map<String, Object>> pm = selector.computeIfAbsent(property, k -> new HashMap<>());
        String cloud = matcher.group(2);

        Map<String, Object> cm = pm.computeIfAbsent(cloud, k -> new HashMap<>());

        String profile = matcher.group(1);
        cm.put(profile, properties.getProperty(key));
    }

    private static Map<String, Object> getLocalizedSelectorProperties(Map<String, Map<String, Map<String, Object>>> selector,
                                                                      String profile, String cloud) {
        Map<String, Object> container = new HashMap<>();

        for (String key : selector.keySet()) {
            Map<String, Map<String, Object>> mapper = selector.get(key);
            Map<String, Object> cm = mapper.get(cloud);

            if (cm != null && cm.containsKey(profile)) {
                container.put(key, cm.get(profile));
            } else {
                cm = mapper.get(DEFAULT_CLOUD);
                if (cm != null) {
                    String _profile = cm.containsKey(profile) ? profile : DEFAULT_CLOUD;
                    container.put(key, cm.get(_profile));
                } else {
                    container.put(key, null);
                }
            }
        }
        return container;
    }
}
