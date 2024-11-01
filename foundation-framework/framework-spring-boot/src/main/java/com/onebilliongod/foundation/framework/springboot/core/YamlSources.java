package com.onebilliongod.foundation.framework.springboot.core;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YamlSources {

    YamlSource[] value();
}
