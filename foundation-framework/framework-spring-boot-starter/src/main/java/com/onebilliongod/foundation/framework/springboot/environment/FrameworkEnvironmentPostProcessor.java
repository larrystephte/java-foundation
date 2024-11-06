package com.onebilliongod.foundation.framework.springboot.environment;

import com.onebilliongod.foundation.commons.core.common.Constants;
import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool;
import com.onebilliongod.foundation.framework.springboot.utils.K8SUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;


import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;


import static com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool.DEFAULT_CHARSET;

/**
 *  FrameworkEnvironmentPostProcessor is used to customize the Spring application environment
 *  during the startup phase. It implements the EnvironmentPostProcessor and Ordered interfaces,
 *  allowing it to modify environment properties and control the order in which it is applied.
 */
public class FrameworkEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final int ORDER = HIGHEST_PRECEDENCE + 100;

    public static final String SOURCE_NAME_FIRST = "framework-boot-env-first";

    public static final String SOURCE_NAME_LAST = "framework-boot-env-last";

    private final Map<String, Object> lastSource = new HashMap<>();

    private final Map<String, Object> firstSource = new HashMap<>();

    public static final String SERVICE_LOG_IN_K8S_PATH = "/data/logs/${K8S_POD_NAMESPACE:default_namespace}/${spring.application.name}/${HOSTNAME}";


    /**
     * Post-processes the Spring application environment by adding custom property sources.
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();

        if (propertySources.contains(SOURCE_NAME_FIRST) || propertySources.contains(SOURCE_NAME_LAST)) {
            return;
        }

        MapPropertySource lastPropertySource = new MapPropertySource(SOURCE_NAME_LAST, lastSource);
        propertySources.addLast(lastPropertySource);

        MapPropertySource firstPropertySource = new MapPropertySource(SOURCE_NAME_FIRST, firstSource);
        propertySources.addFirst(firstPropertySource);

        //resolve logging environment varies
        resolveLogging(environment);

        //
//        resolvePath(environment);

        resolveSpring(environment);


    }

    /**
     * Resolves the logging configuration based on the environment.
     * The method sets up various Logging properties.
     * @param environment
     */
    private void resolveLogging(ConfigurableEnvironment environment) {
        // Set logback configuration files
        lastSourcePut(environment, "logging.config", "classpath:com/onebilliongod/foundation/framework/springboot/logback.xml");
        lastSourcePut(environment, "logback.access.config", "classpath:com/onebilliongod/foundation/framework/springboot/logback-access.xml");

        //Set log file path based on the environment
        lastSourcePut(environment, "logging.file.path", buildLoggingPath(environment));

        //Set other logging properties such as log levels and log rolling policies
        lastSourcePut(environment, "logging.group.foundation",  "com.onebilliongod.foundation.framework.springboot,com.onebilliongod.foundation.framework.springcloud");
        lastSourcePut(environment, "logging.level.foundation", "INFO");
        lastSourcePut(environment, "logging.level.org.springframework.context.support", "WARN");
        lastSourcePut(environment, "logging.logback.rollingpolicy.max-history", "3");
        lastSourcePut(environment, "logging.logback.rollingpolicy.max-file-size", "128MB");
        lastSourcePut(environment, "logging.logback.rollingpolicy.total-size-cap", "12GB");
        lastSourcePut(environment, "logging.charset.file", DEFAULT_CHARSET);//官方default为UTF8
        lastSourcePut(environment, "logging.charset.console", DEFAULT_CHARSET);//官方default为UTF8
    }

    private void resolveFramework(final ConfigurableEnvironment environment) {
        String localIp = NetworkUtils.getLocalIp();
        if (localIp == null) {
            throw new IllegalStateException("The framework could not resolve the local IP address (IPv4). Please review the hosts configuration!");
        }
        lastSourcePut(environment, Constants.LOCAL_IP, localIp);


        String hostName = NetworkUtils.getHostName();
        if (hostName == null) {
            throw new IllegalStateException("The framework could not resolve the local hostname. Please review the hosts configuration!");
        }
        lastSourcePut(environment, Constants.HOST_NAME, hostName);

        String profile = ContextEnvironmentTool.profile(environment);
        if (profile.isEmpty()) {
            throw new IllegalStateException("The current service environment cannot be determined. Please set the Spring active profile.");
        }

        String applicationName = environment.getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            throw new IllegalArgumentException("Please configure spring.application.name");
        }

        lastSourcePut(environment, "spring.banner.location", "banner.txt");
    }

//    private void resolvePath(final ConfigurableEnvironment environment) {
//        String catalinaBase = environment.getProperty("catalina.base");
//        if (catalinaBase == null) {
//            catalinaBase = ".";
//        }
//        firstSource.put("catalina.base", catalinaBase);
//        System.setProperty("catalina.base", catalinaBase);
//        firstSource.put("server.tomcat.basedir", catalinaBase);
//
//
//        String runtimeBase = environment.getProperty("runtime.base");
//        if (runtimeBase == null) {
//            runtimeBase = catalinaBase + "/runtime";
//        }
//        PathUtils.ensureDirectory(runtimeBase);
//        firstSource.put("runtime.base", runtimeBase);
//        System.setProperty("runtime.base", runtimeBase);
//
//
//        String dataBase = environment.getProperty("data.base");
//        if (dataBase == null) {
//            dataBase = catalinaBase + "/data";
//        }
//        PathUtils.ensureDirectory(dataBase);
//        firstSource.put("data.base", dataBase);
//        System.setProperty("data.base", dataBase);
//    }

    private void resolveSpring(final ConfigurableEnvironment environment) {

        boolean debug = environment.getProperty("debug", Boolean.class, false);
        System.setProperty("DEBUG", "" + debug);

        //覆盖默认的spring配置，如果未指定的话
        firstSource.put("spring.jmx.enabled", true);
        firstSource.put("spring.jmx.default-domain", "com.onebilliongod.foundation");

//        lastSourcePut(environment, "spring.aop.auto", true);
//        lastSourcePut(environment, "spring.aop.proxy-target-class", true);

//        lastSourcePut(environment, "spring.http.encoding.charset", DEFAULT_CHARSET);
//        lastSourcePut(environment, "spring.http.encoding.enabled", true);

//        lastSourcePut(environment, "spring.jackson.time-zone", "GMT+08:00");
//        lastSourcePut(environment, "spring.jackson.serialization.write-dates-as-timestamps", true);

        lastSourcePut(environment, "spring.transaction.default-timeout", "30S");

        //Whether to wait for the TaskExecutor to complete current tasks when the application shuts down. By default, this feature is disabled. If you need to wait for tasks to complete, set this option to true.
        lastSourcePut(environment, "spring.task.execution.shutdown.await-termination", true);
        lastSourcePut(environment, "spring.task.execution.shutdown.await-termination-period", "3S");
        lastSourcePut(environment, "spring.task.scheduling.shutdown.await-termination", true);
        lastSourcePut(environment, "spring.task.scheduling.shutdown.await-termination-period", "3S");
    }

    private void resolveServer(final ConfigurableEnvironment environment) {
        String port = environment.getProperty("port");
        if (port == null) {
            port = environment.getProperty("server.port");
        }
        if (port == null) {
            port = "8080";
        }

        firstSource.put("server.shutdown", "graceful");//平滑下线
        lastSourcePut(environment, "spring.lifecycle.timeout-per-shutdown-phase", "15S");
        lastSourcePut(environment, "server.port", port);
        lastSourcePut(environment, "server.compression.enabled", false);

        //安全考虑
        lastSourcePut(environment, "server.max-http-header-size", "8KB");



    }

    /**
     * Adds a property to the `lastSource` map if the property does not already exist in the environment.
     *  This method ensures that the user's custom configuration (e.g., in `bootstrap.properties`) is not overwritten.
     * @param environment
     * @param key
     * @param value
     */
    void lastSourcePut(ConfigurableEnvironment environment, String key, Object value) {
        // Only add the property if it doesn't already exist in the environment
        if (environment.getProperty(key) == null) {
            lastSource.put(key, value);
        }
    }


    /**
     * Builds the log path for the service, differentiating between Kubernetes and VM environments.
     * @param environment
     * @return
     */
    private String buildLoggingPath(Environment environment) {
        //Check if the service is running inside Kubernetes
        if (K8SUtil.serviceInK8s(environment)) {
            final String namespace = K8SUtil.podOfNamespace("default");
            return SERVICE_LOG_IN_K8S_PATH.replace("default_namespace", namespace);
        } else {
            // In a VM environment, construct the log path using profile and group information
            final String profile = ContextEnvironmentTool.profile(environment);
            final String group = ContextEnvironmentTool.group(environment);
            return "/data/logs/${spring.application.name}-" + profile + "-" + group;
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
