package com.xzc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * schedule annotation
 * "@yearly":  "0 0 0 1 1 *"
 * "@monthly": "0 0 0 1 * *"
 * "@weekly":  "0 0 0 * * 0"
 * "@daily":   "0 0 0 * * *"
 * "@hourly":  "0 0 * * * *"
 *
 * @author Ray
 * @date created in 2021/11/9 18:59
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {
    String cron();

    String desc();
}
