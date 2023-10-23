package com.wgymt.proxypool.annotations;

import java.lang.annotation.*;

/**
 * 代理来源标识注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface FetcherType {
    String value();
}
