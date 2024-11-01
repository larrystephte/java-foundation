package com.onebilliongod.foundation.framework.springboot.core;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(YamlSources.class)
public @interface YamlSource {

    String name() default "";

    String[] value();

    boolean ignoreResourceNotFound() default false;

}
