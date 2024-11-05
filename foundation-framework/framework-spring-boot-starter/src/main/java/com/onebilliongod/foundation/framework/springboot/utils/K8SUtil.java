package com.onebilliongod.foundation.framework.springboot.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author shaojieyue
 * @date 2022/11/11
 */
@Slf4j
public class K8SUtil {
    /**
     * The file that stores the pod's namespace in Kubernetes.
     */
    public static final  String k8s_namespace_file = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

    /**
     *  Retrieves the namespace of the current Kubernetes pod.
     *  It reads the namespace from the system file provided by Kubernetes or returns the default value if unable to read the file.
     * @return
     */
    public static String podOfNamespace(String defaultVal) {
        String namespace = defaultVal;
        Path path = Paths.get(k8s_namespace_file);
        try {
            //  Read the contents of the file as a string, assuming UTF-8 encoding, and trim any excess whitespace
            String read = Files.readString(path, StandardCharsets.UTF_8).trim();

            if (!read.isEmpty()) {
                namespace = read;
            }
        } catch (IOException e) {
            log.error("Failed to read Kubernetes namespace file: " + e.getMessage());
        }
        return namespace;
    }

    /**
     * Determines whether the current service is running inside a Kubernetes environment.
     * @param environment
     * @return
     */
    public static boolean serviceInK8s(final Environment environment) {
        final String kubernetesServiceHost = environment.getProperty("KUBERNETES_SERVICE_HOST");
        return kubernetesServiceHost != null && !kubernetesServiceHost.isEmpty();
    }
}
