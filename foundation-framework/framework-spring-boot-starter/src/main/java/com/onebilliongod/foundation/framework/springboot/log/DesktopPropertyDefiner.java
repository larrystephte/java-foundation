package com.onebilliongod.foundation.framework.springboot.log;

import ch.qos.logback.core.PropertyDefinerBase;
import com.onebilliongod.foundation.framework.springboot.utils.ContextEnvironmentTool;


/**
 *
 */

public class DesktopPropertyDefiner extends PropertyDefinerBase {
    /**
     * Get the property value, defined by this property definer
     *
     * @return defined property value
     */
    @Override
    public String getPropertyValue() {
        return ContextEnvironmentTool.isDesktopEnv() + "";
    }
}
