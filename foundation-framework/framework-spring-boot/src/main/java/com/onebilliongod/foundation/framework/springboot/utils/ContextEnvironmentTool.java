package com.onebilliongod.foundation.framework.springboot.utils;


import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import org.springframework.core.env.Environment;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for handling context and environment-related operations.
 * <p>
 * This class provides methods to retrieve the current profile and cloud environment
 * based on system properties, hostnames, and active profiles.
 * </p>
 */
public final class ContextEnvironmentTool {
    public static final String DEFAULT_GROUP_NAME = "default";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String PROFILE = "profile";
    public static final String CLOUD = "cloud";
    public static final String NAMESPACE_APPLICATION = "spring.application";
    public static final String PREFER_PROFILE = "prefer-profile";

    /**
     * Retrieves the current profile from the environment.
     * <p>
     * The profile can be specified directly or inferred from the hostname and active profiles.
     * </p>
     *
     * @param environment The Spring Environment object.
     * @return The determined profile, or "default" if none is found.
     */
    public static String profile(final Environment environment) {
        String source = getProfileProperty(environment);

        if (source == null) {
            String[] profiles = getActiveOrDefaultProfiles(environment);
            source = profiles.length > 0 ? profiles[profiles.length - 1] : DEFAULT_GROUP_NAME; // Default to "default" if no profiles found
        }

        // Split the profile into environment and group components
        final String[] strings = StringUtils.split(source, "-", 2);
        return strings[0]; // Return the environment part
    }

    // Retrieves the profile property from the environment
    private static String getProfileProperty(Environment environment) {
        String source = environment.getProperty(PROFILE);
        if (source == null) {
            source = environment.getProperty(NAMESPACE_APPLICATION + "." + PREFER_PROFILE);
        }
        return source;
    }

    // Gets active profiles or defaults if none are active
    private static String[] getActiveOrDefaultProfiles(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            String hostName = NetworkUtils.getHostName();
            profiles = inferProfilesFromHostName(hostName);
        }
        return profiles;
    }

    // Infers profiles based on the hostname
    private static String[] inferProfilesFromHostName(String hostName) {
        if (hostName.contains(".prod.")) {
            return new String[] { "prod" };
        } else if (hostName.contains(".pre.")) {
            return new String[] { "pre" };
        } else if (hostName.contains(".beta.")) {
            return new String[] { "beta" };
        } else if (hostName.contains(".test.")) {
            return new String[] { "test" };
        } else if (hostName.contains(".dev.")) {
            return new String[] { "dev" };
        }
        return new String[0]; // No profile inferred
    }

    /**
     * Retrieves the current cloud environment from the environment.
     * <p>
     * The cloud environment can be specified directly or inferred from the hostname.
     * </p>
     *
     * @param environment The Spring Environment object.
     * @return The determined cloud environment, or "default" if none is found.
     */
    public static String cloud(final Environment environment) {
        String source = environment.getProperty(CLOUD);
        if (source != null) {
            return source;
        }

        return inferCloudFromHostName(NetworkUtils.getHostName());
    }

    // Infers cloud provider based on the hostname
    private static String inferCloudFromHostName(String hostName) {
        if (hostName.contains(".aws.")) {
            return "aws";
        } else if (hostName.contains(".azure.")) {
            return "azure";
        } else if (hostName.contains(".gcp.")) {
            return "gcp";
        }
        return "default"; // Fallback to default
    }
}
