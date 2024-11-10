package com.onebilliongod.foundation.framework.springboot.httpclient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "http.client")
@Getter
@Setter
public class HttpClientProperties {
    // Configuration properties
    // Whether to use OkHttp client,the other client is httpClients
    private boolean useOkHttp = true;

    // The maximum wait time for the client to establish a connection to the server. If the connection cannot be established within the specified time, a timeout exception will be thrown.
    private int connectTimeout = 500;

    // The maximum wait time to read data from the server. If no response is received within this time, a timeout exception will be thrown.
    private int readTimeout = 2000;

    // The maximum wait time to write request data to the server. If the data is not written within this time, a timeout exception will be thrown.
    private int writeTimeout = 1000;

    // The time that a connection will be kept idle in the connection pool. If the connection is not reused within this time, it will be closed.
    private long keepAliveDuration = 10000;

    // The maximum number of idle connections allowed in the connection pool.
    private int maxIdleConnections = 10;

    // The maximum number of connections allowed in the Apache HttpClient connection pool.
    private int maxTotalConnections = 200;

    // Number of retry attempts for requests
    private int retry_times = 3;

    // Additional headers to be included in requests
    private Map<String, String> additionalHeaders;
}
