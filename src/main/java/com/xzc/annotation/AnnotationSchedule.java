package com.xzc.annotation;

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Ray
 * @date created in 2021/11/9 19:06
 */
@Data
public class AnnotationSchedule {

    private final Class<?> clazz;
    private final Method method;
    private final String cron;
    private final String desc;

    private Object instance;

    public AnnotationSchedule(Class<?> clazz, Method method, String cron, String desc) {
        this.clazz = clazz;
        this.method = method;
        this.cron = cron;
        this.desc = desc;
        this.verifyDefaultConstructor();
    }

    @SneakyThrows
    private void verifyDefaultConstructor() {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers()) &&
                    constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                this.instance = constructor.newInstance();
                return;
            }
        }

        throw new IllegalStateException("not find default constructor");
    }


    public void invoke() {
        try {
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())) {
                method.invoke(null);
            } else {
                method.invoke(instance);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
