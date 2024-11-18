package com.onebilliongod.foundation.framework.springboot.httpclient;

import com.onebilliongod.foundation.commons.core.common.Constants;
import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestHeadersHandler implements ClientHttpRequestInterceptor, RequestInterceptor {
    private static final String HTTP_HEADER_PREFIX = "X-Foundation-";
    private static final String HTTP_HEADER_ORIGIN_PREFIX = HTTP_HEADER_PREFIX + "Origin-";
    private static final String HTTP_HEADER_PROJECT = HTTP_HEADER_ORIGIN_PREFIX + "Project";

    private static final String HTTP_HEADER_CLOUD = HTTP_HEADER_ORIGIN_PREFIX + "Cloud";

    private static final String HTTP_HEADER_GROUP = HTTP_HEADER_ORIGIN_PREFIX + "Group";

    private static final String HTTP_HEADER_ADDRESS = HTTP_HEADER_ORIGIN_PREFIX + "Address";

    private static final String HTTP_HEADER_INVOKE_LINK = HTTP_HEADER_PREFIX + "Invoke-Link";

    private static final String HTTP_HEADER_REQUEST_ID = "X-Request-Id";

    private static final String HTTP_HEADER_REQUEST_SEQUENCE = "X-Request-Sequence";

    private static final String HTTP_HEADER_REQUEST_TYPE = HTTP_HEADER_PREFIX + "Request-Type";

    private Map<String, String> additionalHeaders;

    public RequestHeadersHandler(Map<String, String> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public void handleHeaders() {
        log.info("handle Headers");
        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
        }

        //add project
        String project = System.getProperty(Constants.PROJECT);
        additionalHeaders.put(HTTP_HEADER_PROJECT, project);
        log.info("handle Headers,project:{}", project);

        //add cloud
        String cloud = System.getProperty(Constants.CLOUD);
        additionalHeaders.put(HTTP_HEADER_CLOUD, cloud);
        log.info("handle Headers,cloud:{}", cloud);

        //add group
        String group = System.getProperty(Constants.GROUP);
        additionalHeaders.put(HTTP_HEADER_GROUP, group);
        log.info("handle Headers,group:{}", group);

        //add ip
        additionalHeaders.put(HTTP_HEADER_ADDRESS, NetworkUtils.getLocalIp());
        log.info("handle Headers,ip:{}", NetworkUtils.getLocalIp());

        //add mdc info
        String requestId = MDC.get(Constants.MDC_REQUEST_ID);
        if (requestId != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_ID, requestId);
        }
        log.info("handle Headers,requestId:{}", requestId);

        String requestSequence = MDC.get(Constants.MDC_REQUEST_SEQUENCE);
        if (requestSequence != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_SEQUENCE, requestSequence);
        }
        log.info("handle Headers,requestSequence:{}", requestSequence);

        String invokeLink = MDC.get(Constants.MDC_INVOKE_LINK);
        if (invokeLink != null) {
            additionalHeaders.put(HTTP_HEADER_INVOKE_LINK, invokeLink);
        }
        log.info("handle Headers,invokeLink:{}", invokeLink);

        String requestType = MDC.get(Constants.MDC_REQUEST_TYPE);
        if (requestType != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_TYPE, requestType);
        }
        log.info("handle Headers,requestType:{}", requestType);
    }

    @NotNull
    @Override
    public ClientHttpResponse intercept(@NotNull HttpRequest request, @NotNull byte[] body, ClientHttpRequestExecution execution) throws IOException {
        handleHeaders();
        additionalHeaders.keySet().forEach(it -> {
            request.getHeaders().add(it, additionalHeaders.get(it));
        });
        return execution.execute(request, body);
    }

    @Override
    public void apply(RequestTemplate template) {
        handleHeaders();
        additionalHeaders.keySet().forEach(it -> {
            template.header(it, additionalHeaders.get(it));
        });
    }
}
