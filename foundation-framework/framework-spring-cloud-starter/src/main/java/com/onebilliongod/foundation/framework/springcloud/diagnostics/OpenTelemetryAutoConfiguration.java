package com.onebilliongod.foundation.framework.springcloud.diagnostics;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@AutoConfiguration
@ConditionalOnProperty(name = "otel.enabled", havingValue = "true", matchIfMissing = true)
public class OpenTelemetryAutoConfiguration {

}
