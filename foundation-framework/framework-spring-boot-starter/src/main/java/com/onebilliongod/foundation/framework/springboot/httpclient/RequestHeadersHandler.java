package com.onebilliongod.foundation.framework.springboot.httpclient;

import com.onebilliongod.foundation.commons.core.common.Constants;
import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class RequestHeadersHandler {
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

    public static void handleHeaders(Map<String, String> additionalHeaders) {
        if (additionalHeaders == null) {
            additionalHeaders = new HashMap<>();
        }

        //add project
        String project = System.getProperty(Constants.PROJECT);
        additionalHeaders.put(HTTP_HEADER_PROJECT, project);

        //add cloud
        String cloud = System.getProperty(Constants.CLOUD);
        additionalHeaders.put(HTTP_HEADER_CLOUD, cloud);

        //add group
        String group = System.getProperty(Constants.GROUP);
        additionalHeaders.put(HTTP_HEADER_GROUP, group);

        //add ip
        additionalHeaders.put(HTTP_HEADER_ADDRESS, NetworkUtils.getLocalIp());

        //add mdc info
        String requestId = MDC.get(Constants.MDC_REQUEST_ID);
        if (requestId != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_ID, requestId);
        }
        String requestSequence = MDC.get(Constants.MDC_REQUEST_SEQUENCE);
        if (requestSequence != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_SEQUENCE, requestSequence);
        }
        String invokeLink = MDC.get(Constants.MDC_INVOKE_LINK);
        if (invokeLink != null) {
            additionalHeaders.put(HTTP_HEADER_INVOKE_LINK, invokeLink);
        }
        String requestType = MDC.get(Constants.MDC_REQUEST_TYPE);
        if (requestType != null) {
            additionalHeaders.put(HTTP_HEADER_REQUEST_TYPE, requestType);
        }
    }
}
