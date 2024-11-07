package com.onebilliongod.foundation.framework.springboot.utils;


import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import org.apache.commons.lang3.SystemUtils;
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

    /**
     * Retrieves the current profile from the environment.
     * <p>
     * The profile can be specified directly or inferred from the hostname and active profiles.
     * For a busy business line, different branches of the same service might be deployed simultaneously for testing.
     * Using "group" to distinguish different testing branches (or service groups) can help organize deployments.
     * This approach also supports future deployments on cloud platforms.
     * </p>
     *
     * @param environment The Spring Environment object.
     * @return The determined profile, or "default" if none is found.
     */
    public static String profile(final Environment environment) {
        String source = environment.getProperty(PROFILE);
        if (source == null) {
            String[] profiles = getActiveOrDefaultProfiles(environment);
            source = profiles.length > 0 ? profiles[profiles.length - 1] : DEFAULT_GROUP_NAME; // Default to "default" if no profiles found
        }

        // Split the profile into environment and group components
        final String[] strings = StringUtils.split(source, "-", 2);
        return strings[0]; // Return the environment part
    }

    /**
     * Retrieves the group that the current running service belongs to.
     *
     * The profile is assumed to have the structure: ${env}-${group}, where ${env} is the environment, and ${group} is the service group.
     *
     * @param environment
     * @return
     */
    public static String group(final Environment environment) {
        //Attempt to retrieve the service group from the configuration property "foundation.service.group"
        final String group = environment.getProperty("foundation.service.group");
        if (StringUtils.isNotBlank(group)) {
            return group;
        }
        String profile = environment.getProperty(PROFILE);
        if (StringUtils.isBlank(profile)) {
            String[] profiles = environment.getActiveProfiles();
            if (profiles == null || profiles.length < 1) {
                return DEFAULT_GROUP_NAME;
            } else {
                profile = profiles[0];
            }
        }

        // The active profile is expected to follow the structure: ${env}-${group}
        final String[] strings = StringUtils.split(profile, "-",2);
        if (strings == null || strings.length != 2) {
            return DEFAULT_GROUP_NAME;
        }

        return strings[1];
    }

    // Gets active profiles or defaults if none are active
    private static String[] getActiveOrDefaultProfiles(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) {
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

    /**
     * determines if the current environment is a desktop environment.
     * This check distinguishes between desktop and server environments
     * based on the operating system and desktop session environment variables.
     * @return true: a desktop environment false: server environments
     */
    public static boolean isDesktopEnv() {
        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC) {
            return true;
        }
        //https://unix.stackexchange.com/questions/78210/how-do-i-find-the-running-gui-environment-from-the-terminal
        return StringUtils.isNotBlank(System.getenv("XDG_CURRENT_DESKTOP")) ||
                StringUtils.isNotBlank(System.getenv("DESKTOP_SESSION"));
    }

}
