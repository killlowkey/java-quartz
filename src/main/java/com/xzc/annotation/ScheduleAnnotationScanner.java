package com.xzc.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2021/11/9 19:01
 */
public class ScheduleAnnotationScanner {

    public List<List<AnnotationSchedule>> scan(List<Class<?>> classList) {
        return classList.stream().map(this::scan)
                .collect(Collectors.toList());
    }

    public List<AnnotationSchedule> scan(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");

        List<AnnotationSchedule> result = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Schedule.class)) {
                continue;
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalStateException(method.getName() + " method is not public");
            }

            if (method.getParameterCount() > 0) {
                throw new IllegalStateException(method.getName() + " method have argument");
            }

            if (method.getReturnType() != Void.TYPE) {
                throw new IllegalStateException(method.getName() + "method return type not void");
            }

            Schedule schedule = method.getAnnotation(Schedule.class);
            AnnotationSchedule entity = new AnnotationSchedule(clazz, method, schedule.cron(), schedule.desc());
            result.add(entity);
        }

        return result;
    }
}
